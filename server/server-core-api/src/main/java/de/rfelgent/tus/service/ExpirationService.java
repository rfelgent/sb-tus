package de.rfelgent.tus.service;

import de.rfelgent.tus.domain.Asset;

import java.util.Date;

/**
 *
 * @author rfelgentraeger
 */
public interface ExpirationService {

    /**
     * Calculates a date when the asset expires.
     *
     * Any expired asset is not retrievable by clients for any upload or update action.
     *
     * @param asset
     * @return
     */
    Date expireDate(Asset asset);
}
