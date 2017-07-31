package de.rfelgent.tus.service;

import de.rfelgent.tus.domain.LockException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author rfelgentraeger
 */
public class AssetLockerInMemory implements AssetLocker {

    private Map<String, Boolean> locks = new ConcurrentHashMap<>();

    @Override
    public void lock(String referenceId) throws LockException {
        if (locks.get(referenceId) != null) {
            throw new LockException("Upload is already locked");
        }

        locks.put(referenceId, Boolean.TRUE);
    }

    @Override
    public void release(String referenceId) throws LockException {
        locks.remove(referenceId);
    }


}
