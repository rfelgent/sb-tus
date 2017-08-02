package de.rfelgent.tus.service;

import de.rfelgent.tus.domain.Asset;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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

    default String toRFC7231Format(Date expirationDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(expirationDate);
    }
}
