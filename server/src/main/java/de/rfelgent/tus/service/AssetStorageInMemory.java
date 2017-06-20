package de.rfelgent.tus.service;

import de.rfelgent.tus.domain.Asset;
import de.rfelgent.tus.domain.AssetStatus;
import de.rfelgent.tus.domain.StorageException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author rfelgentraeger
 */
@Service
public class AssetStorageInMemory implements AssetStorage {

    /* created assets */
    private Map<String, Asset> assets = new ConcurrentHashMap();

    /** uploaded bytes */
    private Map<String, byte[]> uploads = new ConcurrentHashMap();

    /** running uploads */
    private Set<String> runningUploads = new HashSet();

    @Override
    public void terminate(String referenceId) {
        assets.remove(referenceId);
        runningUploads.remove(referenceId);
        uploads.remove(referenceId);
    }

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
    public void write(String referenceId, InputStream is) throws StorageException {
        //Note: the UploadLocker is responsible for avoiding concurrent uploads for same asset
        //if (uploads.containsKey(upload.getAssetReferenceId())) {
        //    throw new StorageException("There is already an Upload in progress");
        //}

        ByteArrayOutputStream output = null;
        byte[] uploadedData = null;
        try {
            runningUploads.add(referenceId);
            output = new ByteArrayOutputStream();
            IOUtils.copy(is, output);
        } catch (IOException e) {
            //Exceptions are "ok" ==> examine output for uploaded data
            //throw new StorageException("upload data not readable", e);
        } finally {
            if (output != null) {
                uploadedData = output.toByteArray();
            }
            IOUtils.closeQuietly(output);
            runningUploads.remove(referenceId);
        }

        byte[] previouslyUploadedData = uploads.get(referenceId);
        if  (previouslyUploadedData == null) {
            previouslyUploadedData = new byte[0];
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(previouslyUploadedData);
            baos.write(uploadedData);
            uploads.put(referenceId, baos.toByteArray());
        } catch (IOException ioe) {
            //should not happen
            throw new StorageException("Storing uploaded data failed", ioe);
        }
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
        assetStatus.setUploading(runningUploads.contains(referenceId));
        byte[] uploadedData = uploads.get(referenceId);
        if (uploadedData == null) {
            uploadedData = new byte[0];
        }
        assetStatus.setUploadedSize(uploadedData.length);
        assetStatus.setLocked(false);

        return assetStatus;
    }
}
