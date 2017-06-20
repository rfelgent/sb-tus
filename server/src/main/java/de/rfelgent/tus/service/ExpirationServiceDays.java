package de.rfelgent.tus.service;

import de.rfelgent.tus.domain.Asset;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author rfelgentraeger
 */
public class ExpirationServiceDays implements ExpirationService {

    //TUS spec recommends one week expiration policy.
    private int days = 7;

    @Override
    public Date expireDate(Asset asset) {
        long futureDays = TimeUnit.DAYS.toMillis(getDays());
        return new Date(asset.getCreationDate().getTime() + futureDays);
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }
}
