package de.rfelgent.tus.service;

import de.rfelgent.tus.domain.Asset;

import java.util.Date;

/**
 * The {@link de.rfelgent.tus.TusHeaders#UPLOAD_EXPIRES} header MUST be included in every PATCH response if the upload is going to expire.
 * If the expiration is known at the creation, the Upload-Expires header MUST be included in the response to
 * the initial POST request. Its value MAY change over time.
 *
 * @author rfelgentraeger
 */
public interface ExpirationService {

    Date expireDate(Asset asset);
}
