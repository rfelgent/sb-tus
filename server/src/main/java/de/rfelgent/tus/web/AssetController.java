package de.rfelgent.tus.web;

import de.rfelgent.tus.Headers;
import de.rfelgent.tus.domain.Asset;
import de.rfelgent.tus.service.AssetFactory;
import de.rfelgent.tus.service.AssetService;
import de.rfelgent.tus.service.UploadLocationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author rfelgentraeger
 */
@RestController
@RequestMapping("/files")
public class AssetController {

    @Autowired
    private AssetService assetService;
    @Autowired
    private UploadLocationResolver uploadLocationResolver;
    @Autowired
    private AssetFactory assetFactory;

    @PostMapping(value = {"", "/"})
    public ResponseEntity<Void> init(
            @RequestHeader(value = Headers.UPLOAD_LENGTH, required = false) Long uploadSize,
            @RequestHeader(value = Headers.UPLOAD_META, required = false) String uploadMeta) throws MalformedURLException {

        Asset asset = assetFactory.newInstance();
        asset.setUploadedSize(uploadSize);
        asset.setMetaFromMetaHttpHeader(uploadMeta);

        URL location = uploadLocationResolver.resolve(asset);

        return ResponseEntity.noContent()
                .header("Location", location.toString()).build();
    }

    @RequestMapping(value = {"/{id}", "/{id}/"}, method = {RequestMethod.PATCH})
    public ResponseEntity<Void> upload(@RequestHeader(value = "Content-Type") String contentType,
                       @RequestHeader(value = Headers.UPLOAD_OFFSET) Long offset,
                       @PathVariable(value = "id") String id) {
        if (!"application/offset+octet-stream".equalsIgnoreCase(contentType)) {
            //TODO: error handling
        }

        Asset asset = assetService.findAsset(id);
        if (asset == null) {
            //TODO: error handling
        }
        if (asset.getUploadedSize() != offset) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        //TODO: do the save logic

        asset = assetService.findAsset(id);
        return ResponseEntity.noContent()
                .header(Headers.UPLOAD_OFFSET, asset.getUploadedSize() + "")
                .build();
    }


    @RequestMapping(value = {"/{id}", "/{id}/"}, method = {RequestMethod.HEAD})
    public ResponseEntity<Void> status(@PathVariable String id) {

        Asset asset = assetService.findAsset(id);
        if (asset == null) {
            return ResponseEntity
                    .notFound()
                    .cacheControl(CacheControl.noStore())
                    .build();
        }

        ResponseEntity.HeadersBuilder builder = ResponseEntity.status(HttpStatus.OK)
                .cacheControl(CacheControl.noStore())
                .header(Headers.UPLOAD_OFFSET, asset.getUploadedSize() + "");
        if (asset.getTotalSize() != null) {
                builder.header(Headers.UPLOAD_LENGTH, asset.getTotalSize() + "");
        }
        return builder.build();
    }
}
