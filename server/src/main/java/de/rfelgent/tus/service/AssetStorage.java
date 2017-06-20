package de.rfelgent.tus.service;

import de.rfelgent.tus.domain.Asset;
import de.rfelgent.tus.domain.AssetStatus;
import de.rfelgent.tus.domain.Upload;
import de.rfelgent.tus.domain.StorageException;

import java.net.URL;

/**
 * @author rfelgentraeger
 */
public interface AssetStorage {

    Asset find(String referenceId);

    /**
     * @param asset
     * @throws StorageException
     */
    void init(Asset asset) throws StorageException;

    void write(Upload upload) throws StorageException;

    /**
     * @param referenceId
     * @return may be <code>null</code> if the Asset does not exist.
     */
    AssetStatus status(String referenceId);
}
