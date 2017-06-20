package de.rfelgent.tus.service;

import de.rfelgent.tus.domain.Asset;

import java.util.Collections;
import java.util.Date;

/**
 * Dedicated class for creating an instance of {@link de.rfelgent.tus.domain.Asset}.
 *
 * @author rfelgentraeger
 */
public class AssetFactory {

    public Asset newInstance() {
        Asset asset = new Asset();
        asset.setCreationDate(new Date());
        asset.setMeta(Collections.EMPTY_MAP);

        return asset;
    }
}
