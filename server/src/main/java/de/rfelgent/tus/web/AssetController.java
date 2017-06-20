package de.rfelgent.tus.web;

import de.rfelgent.tus.TusHeaders;
import de.rfelgent.tus.domain.Asset;
import de.rfelgent.tus.domain.AssetNotFoundException;
import de.rfelgent.tus.domain.AssetStatus;
import de.rfelgent.tus.domain.LockException;
import de.rfelgent.tus.domain.Upload;
import de.rfelgent.tus.domain.StorageException;
import de.rfelgent.tus.event.AssetCreatedEvent;
import de.rfelgent.tus.service.AssetFactory;
import de.rfelgent.tus.service.AssetStorage;
import de.rfelgent.tus.service.UploadLocationResolver;
import de.rfelgent.tus.service.UploadLocker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

/**
 * @author rfelgentraeger
 */
@RestController
@RequestMapping("/files")
public class AssetController {

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

    @PostMapping(value = {"", "/"})
    public ResponseEntity<Void> init(
            @RequestHeader(value = TusHeaders.UPLOAD_LENGTH, required = false) Long uploadSize,
            @RequestHeader(value = TusHeaders.UPLOAD_META, required = false) String uploadMeta) throws StorageException {

        Asset asset = assetFactory.newInstance();
        asset.setTotalSize(uploadSize);
        asset.setMetaFromMetaHttpHeader(uploadMeta);

        assetStorage.init(asset);

        URL location;
        try {
            location = uploadLocationResolver.resolve(asset);
        } catch (MalformedURLException e) {
            //TODO: handle orphaned resources (there is no URL to reference it!)
            throw new RuntimeException("Location resolving failed");
        }
        publisher.publishEvent(new AssetCreatedEvent(asset));

        return ResponseEntity.noContent()
                .header("Location", location.toString()).build();
    }

    @RequestMapping(value = {"/{id}", "/{id}/"}, method = {RequestMethod.PATCH})
    public ResponseEntity<Void> upload(@RequestHeader(value = "Content-Type", required = false) String contentType,
                                       @RequestHeader(value = TusHeaders.UPLOAD_OFFSET, required = false) Long offset,
                                       @PathVariable(value = "id") String id,
                                       HttpServletRequest request) throws AssetNotFoundException, IOException, StorageException, LockException {
        if (contentType == null || offset == null
                || !"application/offset+octet-stream".equalsIgnoreCase(contentType)) {
            throw new IllegalArgumentException("Invalid request");
        }

        Asset asset = assetStorage.find(id);
        if (asset == null) {
            throw new AssetNotFoundException();
        }

        AssetStatus assetStatus = assetStorage.status(asset.getReferenceId());
        if (assetStatus.getUploadedSize() != offset) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Upload upload = new Upload();
        upload.setAssetReferenceId(asset.getReferenceId());
        upload.setInputStream(request.getInputStream());
        upload.setOffset(offset);

        try {
            uploadLocker.lock(upload.getAssetReferenceId());
            assetStorage.write(upload);
        } finally {
            uploadLocker.release(upload.getAssetReferenceId());
        }

        assetStatus = assetStorage.status(asset.getReferenceId());
        return ResponseEntity.noContent()
                .header(TusHeaders.UPLOAD_OFFSET, assetStatus.getUploadedSize() + "")
                .build();
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
        if (asset.getTotalSize() != null) {
                builder.header(TusHeaders.UPLOAD_LENGTH, asset.getTotalSize() + "");
        }
        return builder.build();
    }
}
