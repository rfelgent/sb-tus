package de.rfelgent.tus.web;

import de.rfelgent.tus.TusHeaders;
import de.rfelgent.tus.domain.Asset;
import de.rfelgent.tus.domain.AssetNotFoundException;
import de.rfelgent.tus.domain.AssetStatus;
import de.rfelgent.tus.domain.LockException;
import de.rfelgent.tus.domain.StorageException;
import de.rfelgent.tus.service.AssetStorage;
import de.rfelgent.tus.service.AssetLocker;
import de.rfelgent.tus.service.ExpirationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;

/**
 * @author rfelgentraeger
 */
@RestController
@RequestMapping("/files")
public class UploadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    private AssetStorage assetStorage;
    @Autowired
    private AssetLocker assetLocker;
    @Autowired
    private ExpirationService expirationService;

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
        if (asset.getExpirationDate() != null &&
                asset.getExpirationDate().before(new Date())) {
            LOGGER.warn("The asset {} is not updatable due to expiration date", asset.getReferenceId());
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
            assetLocker.lock(asset.getReferenceId());

            AssetStatus assetStatus = assetStorage.status(asset.getReferenceId());
            if (assetStatus.getUploadedSize() != offset) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            assetStorage.write(asset.getReferenceId(), request.getInputStream());

            //update the status after previous write attempt
            assetStatus = assetStorage.status(asset.getReferenceId());

            HttpHeaders headers = new HttpHeaders();
            headers.set(TusHeaders.UPLOAD_OFFSET, Long.toString(assetStatus.getUploadedSize()));
            if (asset.getExpirationDate() != null) {
                headers.set(TusHeaders.UPLOAD_EXPIRES, expirationService.toRFC7231Format(asset.getExpirationDate()));
            }
            return ResponseEntity.noContent().headers(headers).build();
        } finally {
            assetLocker.release(asset.getReferenceId());
        }
    }
}
