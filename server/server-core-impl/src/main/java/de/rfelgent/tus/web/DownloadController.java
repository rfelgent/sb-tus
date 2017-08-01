package de.rfelgent.tus.web;

import de.rfelgent.tus.domain.Asset;
import de.rfelgent.tus.domain.AssetNotFoundException;
import de.rfelgent.tus.domain.AssetStatus;
import de.rfelgent.tus.domain.LockException;
import de.rfelgent.tus.domain.StorageException;
import de.rfelgent.tus.service.AssetStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author rfelgentraeger
 */
@RestController
@RequestMapping("/files")
public class DownloadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadController.class);

    @Autowired
    private AssetStorage assetStorage;

    @GetMapping(value = {"/{id}", "/{id}/"})
    public ResponseEntity<InputStreamResource> download(@PathVariable(value = "id") String id) throws IOException, StorageException, LockException {
        Asset asset = assetStorage.find(id);
        if (asset == null) {
            throw new AssetNotFoundException();
        }

        AssetStatus status = assetStorage.status(asset.getReferenceId());

        boolean canDownload = true;
        if (status.isUploading()) {
            LOGGER.debug("A currently running upload for asset {} prevents from download!", id);
            canDownload = false;
        }

        if (asset.getTotalSize() != null
                && asset.getTotalSize() != status.getUploadedSize()) {
            LOGGER.debug("The asset {} is not fully uploaded which prevents from download!", id);
            canDownload = false;
        }

        if (canDownload) {
            InputStream is = assetStorage.getStream(id);

            ResponseEntity.BodyBuilder bb = ResponseEntity.ok()
                    .contentLength(status.getUploadedSize());
                    //TODO content-type
                    //.contentType(XXX)
            return bb.body(new InputStreamResource(is));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
