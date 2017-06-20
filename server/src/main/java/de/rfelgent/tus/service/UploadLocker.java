package de.rfelgent.tus.service;

import de.rfelgent.tus.domain.LockException;

/**
 * @author rfelgentraeger
 */
public interface UploadLocker {

    void lock(String referenceId) throws LockException;

    /**
     * Unlocks the given resource. Not locked resources are silently ignored.
     *
     * @param referenceId
     * @throws LockException
     */
    void release(String referenceId) throws LockException;
}
