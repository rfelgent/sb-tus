package de.rfelgent.tus.web;

import de.rfelgent.tus.TusHeaders;
import de.rfelgent.tus.domain.Asset;
import de.rfelgent.tus.domain.AssetNotFoundException;
import de.rfelgent.tus.domain.AssetStatus;
import de.rfelgent.tus.domain.LockException;
import de.rfelgent.tus.domain.StorageException;
import de.rfelgent.tus.event.AssetCreatedEvent;
import de.rfelgent.tus.event.AssetTerminatedEvent;
import de.rfelgent.tus.service.AssetFactory;
import de.rfelgent.tus.service.AssetStorage;
import de.rfelgent.tus.service.ExpirationService;
import de.rfelgent.tus.service.UploadLocationResolver;
import de.rfelgent.tus.service.UploadLocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

/**
 * @author rfelgentraeger
 */
@RestController
@RequestMapping("/files")
public class AssetController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetController.class);

    @Autowired
    private AssetStorage assetStorage;
    @Autowired
    private AssetFactory assetFactory;
    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private UploadLocker uploadLocker;
    @Autowired
    private UploadLocationResolver uploadLocationResolver;
    @Autowired
    private ExpirationService expirationService;

    @PostMapping(value = {"", "/"})
    public ResponseEntity<Void> init(
            @RequestHeader(value = TusHeaders.UPLOAD_LENGTH, required = false) Long uploadSize,
            @RequestHeader(value = TusHeaders.UPLOAD_META, required = false) String uploadMeta) throws StorageException {

        Asset asset = assetFactory.newInstance();
        //some clients handle "unknown upload size/deferred upload" with an upload length equal to 0, e.g. tus-java-clien !
        if (uploadSize != null && uploadSize > 0) {
            asset.setTotalSize(uploadSize);
        }
        asset.setMetaFromMetaHttpHeader(uploadMeta);

        assetStorage.init(asset);

        String location;
        try {
            location = uploadLocationResolver.resolve(asset);
        } catch (Exception e) {
            //TODO: handle orphaned resources (there is no URL to reference it!) Maybe TUS expiration feature?
            throw new RuntimeException("Location resolving failed");
        }
        publisher.publishEvent(new AssetCreatedEvent(asset, location));

        ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.CREATED);
        Date expirationDate = expirationService.expireDate(asset);
        if (expirationDate != null) {
            //format RFC 7231
            builder.header(TusHeaders.UPLOAD_EXPIRES, DateFormat.getDateInstance().format(expirationDate));
        }
        builder.header("Location", location.toString());

        return builder.build();
    }

    @DeleteMapping(value= {"/{id}", "/{id}/"})
    public ResponseEntity<Void> terminate(@PathVariable(value = "id") String id) throws LockException {
        Asset asset = assetStorage.find(id);
        if (asset != null) {
            try {
                //locking can fail if there is a current upload going on. what is the correct behaviour? force cancel?
                uploadLocker.lock(asset.getReferenceId());
                assetStorage.terminate(id);
                publisher.publishEvent(new AssetTerminatedEvent(asset));
            } finally {
                uploadLocker.release(asset.getReferenceId());
            }
        }
        return ResponseEntity.noContent().build();
    }


    @RequestMapping(value = {"/{id}", "/{id}/"}, method = {RequestMethod.PATCH})
    public ResponseEntity<Void> upload(@RequestHeader(value = "Content-Type", required = false) String contentType,
                                       @RequestHeader(value = TusHeaders.UPLOAD_OFFSET, required = false) Long offset,
                                       @RequestHeader(value = TusHeaders.UPLOAD_LENGTH, required = false) Long uploadLength,
                                       @RequestHeader(value = TusHeaders.UPLOAD_META, required = false) String uploadMeta,
                                       @PathVariable(value = "id") String id,
                                       HttpServletRequest request) throws IOException, StorageException, LockException {
        if (contentType == null || offset == null
                || !"application/offset+octet-stream".equalsIgnoreCase(contentType)) {
            throw new IllegalArgumentException("Invalid request");
        }

        Asset asset = assetStorage.find(id);
        if (asset == null) {
            throw new AssetNotFoundException();
        }

        if (asset.getTotalSize() == null
                && uploadLength != null && uploadLength > 0) {
            asset.setTotalSize(uploadLength);
            //TODO: explicitly "save" changed entity
        }

        if (uploadMeta != null) {
            LOGGER.warn("Updating meta data during upload is not supported by this implementation! Not handling: {}", uploadMeta);
        }

        try {
            uploadLocker.lock(asset.getReferenceId());

            AssetStatus assetStatus = assetStorage.status(asset.getReferenceId());
            if (assetStatus.getUploadedSize() != offset) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            assetStorage.write(asset.getReferenceId(), request.getInputStream());

            //update the status after previous write attempt
            assetStatus = assetStorage.status(asset.getReferenceId());

            return ResponseEntity.noContent()
                    .header(TusHeaders.UPLOAD_OFFSET, assetStatus.getUploadedSize() + "")
                    .build();
        } finally {
            uploadLocker.release(asset.getReferenceId());
        }
    }


    @RequestMapping(value = {"/{id}", "/{id}/"}, method = {RequestMethod.HEAD})
    public ResponseEntity<Void> status(@PathVariable String id) {
        Asset asset = assetStorage.find(id);
        if (asset == null) {
            return ResponseEntity
                    .notFound()
                    .cacheControl(CacheControl.noStore())
                    .build();
        }

        AssetStatus assetStatus = assetStorage.status(asset.getReferenceId());
        ResponseEntity.HeadersBuilder builder = ResponseEntity.status(HttpStatus.OK)
                .cacheControl(CacheControl.noStore())
                .header(TusHeaders.UPLOAD_OFFSET, assetStatus.getUploadedSize() + "");
        //according to the TUS protocol the expiration date may change ==> asking service again
        Date expirationDate = expirationService.expireDate(asset);
        if (expirationDate != null) {
            //format RFC 7231
            builder.header(TusHeaders.UPLOAD_EXPIRES,  DateFormat.getDateInstance().format(expirationDate));
        }
        if (asset.getTotalSize() != null) {
            builder.header(TusHeaders.UPLOAD_LENGTH, asset.getTotalSize() + "");
        } else {
            builder.header(TusHeaders.UPLOAD_DEFER_LENGTH, "1");
        }
        return builder.build();
    }
}
