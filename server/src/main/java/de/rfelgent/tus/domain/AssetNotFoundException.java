package de.rfelgent.tus.domain;

/**
 * @author rfelgentraeger
 */
public class AssetNotFoundException extends StorageException {

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
