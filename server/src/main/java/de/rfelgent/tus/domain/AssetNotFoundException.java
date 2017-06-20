package de.rfelgent.tus.domain;

/**
 * @author rfelgentraeger
 */
public class AssetNotFoundException extends Exception {

    public AssetNotFoundException() {
        super();
    }

    public AssetNotFoundException(String message) {
        super(message);
    }

    public AssetNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
