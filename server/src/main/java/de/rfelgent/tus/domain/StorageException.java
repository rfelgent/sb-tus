package de.rfelgent.tus.domain;

/**
 * @author rfelgentraeger
 */
public class StorageException extends Exception {

    public StorageException() {
    }

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
