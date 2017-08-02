package de.rfelgent.tus.web;

import de.rfelgent.tus.TusHeaders;
import de.rfelgent.tus.domain.Asset;
import de.rfelgent.tus.domain.AssetStatus;
import de.rfelgent.tus.domain.LockException;
import de.rfelgent.tus.domain.StorageException;
import de.rfelgent.tus.event.AssetCreatedEvent;
import de.rfelgent.tus.event.AssetTerminatedEvent;
import de.rfelgent.tus.service.AssetFactory;
import de.rfelgent.tus.service.AssetLocker;
import de.rfelgent.tus.service.AssetStorage;
import de.rfelgent.tus.service.ExpirationService;
import de.rfelgent.tus.service.LocationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    private AssetLocker uploadLocker;
    @Autowired
    private LocationResolver locationResolver;
    @Autowired
    private ExpirationService expirationService;

    @PostMapping(value = {"", "/"})
    public ResponseEntity<Void> init(
            @RequestHeader(value = TusHeaders.UPLOAD_LENGTH, required = false) Long uploadSize,
            @RequestHeader(value = TusHeaders.UPLOAD_META, required = false) String uploadMeta) throws StorageException {

        Asset asset = assetFactory.newInstance();
        //some clients handle "unknown upload size/deferred upload" with an upload length equal to 0, e.g. tus-java-client !
        asset.setTotalSize(uploadSize != null && uploadSize > 0 ? uploadSize : null);
        asset.setMetaFromMetaHttpHeader(uploadMeta);

        assetStorage.init(asset);

        String location;
        try {
            location = locationResolver.resolve(asset);
        } catch (Exception e) {
            //TODO: handle orphaned resources (there is no URL to reference it!) Maybe TUS expiration feature?
            throw new RuntimeException("Location resolving failed");
        }
        publisher.publishEvent(new AssetCreatedEvent(asset, location));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Location", location.toString());
        if (asset.getExpirationDate() != null) {
            headers.set(TusHeaders.UPLOAD_EXPIRES, expirationService.toRFC7231Format(asset.getExpirationDate()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).build();
    }

    @DeleteMapping(value= {"/{id}", "/{id}/"})
    public ResponseEntity<Void> delete(@PathVariable(value = "id") String id) throws LockException {
        Asset asset = assetStorage.find(id);
        if (asset != null) {
            try {
                //TODO: handle locking failure in case of running upload. What is the correct behaviour? Force cancel?
                uploadLocker.lock(asset.getReferenceId());
                assetStorage.terminate(id);
                publisher.publishEvent(new AssetTerminatedEvent(asset));
            } finally {
                uploadLocker.release(asset.getReferenceId());
            }
        }
        return ResponseEntity.noContent().build();
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

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noStore().getHeaderValue());
        headers.set(TusHeaders.UPLOAD_OFFSET, Long.toString(assetStatus.getUploadedSize()));
        if (asset.getExpirationDate() != null) {
            headers.set(TusHeaders.UPLOAD_EXPIRES, expirationService.toRFC7231Format(asset.getExpirationDate()));
        }
        if (asset.getTotalSize() != null) {
            headers.set(TusHeaders.UPLOAD_LENGTH, Long.toString(asset.getTotalSize()));
        } else {
            headers.set(TusHeaders.UPLOAD_DEFER_LENGTH, "1");
        }
        String metaHeader = asset.toMetaHttpHeader();
        if (!metaHeader.isEmpty()) {
            headers.set(TusHeaders.UPLOAD_META, metaHeader);
        }
        return ResponseEntity.status(HttpStatus.OK).headers(headers).build();
    }
}
