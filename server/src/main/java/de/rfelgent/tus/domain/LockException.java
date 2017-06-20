package de.rfelgent.tus.domain;

/**
 * @author rfelgentraeger
 */
public class LockException extends Exception {

    public LockException(String message) {
        super(message);
    }

    public LockException(String message, Throwable cause) {
        super(message, cause);
    }
}
