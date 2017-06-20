package de.rfelgent.tus.service;

import de.rfelgent.tus.domain.LockException;

import java.util.HashSet;
import java.util.Set;

/**
 * @author rfelgentraeger
 */
public class UploadLockerInMemory implements UploadLocker {

    private Set<String> locks = new HashSet();

    @Override
    public void lock(String referenceId) throws LockException {
        if (locks.contains(referenceId)) {
            throw new LockException("Upload is already locked");
        }

        locks.add(referenceId);
    }

    @Override
    public void release(String referenceId) throws LockException {
        locks.remove(referenceId);
    }


}
