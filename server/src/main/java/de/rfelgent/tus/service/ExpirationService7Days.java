package de.rfelgent.tus.service;

import de.rfelgent.tus.domain.Asset;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * TUS spec recommends one week expiration strategy. This is the default implementation of the recommendation.
 *
 * @author rfelgentraeger
 */
public class ExpirationService7Days implements ExpirationService {

    @Override
    public Date expireDate(Asset asset) {
        long future7Days = TimeUnit.DAYS.toMillis(7);
        return new Date(asset.getCreationDate().getTime() + future7Days);
    }
}
