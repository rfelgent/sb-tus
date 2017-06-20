package de.rfelgent.tus.service;

import de.rfelgent.tus.domain.Asset;
import de.rfelgent.tus.domain.AssetStatus;
import de.rfelgent.tus.domain.StorageException;
import de.rfelgent.tus.domain.Upload;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author rfelgentraeger
 */
@Service
public class AssetStorageInMemory implements AssetStorage {

    /* in memory store of the created assets */
    private Map<String, Asset> assets = new ConcurrentHashMap();

    @Override
    public Asset find(String referenceId) {
        return assets.get(referenceId);
    }

    @Override
    public void init(Asset asset) throws StorageException {
        if (find(asset.getReferenceId()) != null) {
            throw new StorageException("Asset already exists");
        }

        assets.put(asset.getReferenceId(), asset);
    }

    @Override
    public void write(Upload upload) throws StorageException {
        throw new StorageException("Not implemented yet");
    }

    /**
     *
     * @param referenceId
     * @return may return <code>null</code> if there is no instance of {@link Asset} associated with the given reference id
     */
    @Override
    public AssetStatus status(String referenceId) {
        Asset asset = find(referenceId);
        if (asset == null) {
            return null;
        }
        AssetStatus assetStatus = new AssetStatus();
        assetStatus.setUploading(false);
        assetStatus.setUploadedSize(0);
        assetStatus.setLocked(false);

        return assetStatus;
    }
}
