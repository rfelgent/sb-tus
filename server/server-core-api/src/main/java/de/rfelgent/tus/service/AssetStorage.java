package de.rfelgent.tus.service;

import de.rfelgent.tus.domain.Asset;
import de.rfelgent.tus.domain.AssetStatus;
import de.rfelgent.tus.domain.StorageException;

import java.io.InputStream;

/**
 * @author rfelgentraeger
 */
public interface AssetStorage {

    /**
     * @param referenceId
     */
    void terminate(String referenceId);

    Asset find(String referenceId);

    /**
     * @param asset
     * @throws StorageException
     */
    void init(Asset asset) throws StorageException;

    void write(String referenceId, InputStream is) throws StorageException;

    InputStream getStream(String referenceId) throws StorageException;

    /**
     * @param referenceId
     * @return may be <code>null</code> if the Asset does not exist.
     */
    AssetStatus status(String referenceId);
}
