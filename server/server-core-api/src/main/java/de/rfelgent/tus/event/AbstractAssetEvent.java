package de.rfelgent.tus.event;

import de.rfelgent.tus.domain.Asset;

/**
 * @author rfelgentraeger
 */
public abstract class AbstractAssetEvent {

    private Asset asset;

    protected AbstractAssetEvent(Asset asset) {
        this.asset = asset;
    }

    public Asset getAsset() {
        return asset;
    }
}
