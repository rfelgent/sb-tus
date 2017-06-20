package de.rfelgent.tus.event;

import de.rfelgent.tus.domain.Asset;

/**
 * @author rfelgentraeger
 */
public class AssetTerminatedEvent {

    private Asset asset;

    public AssetTerminatedEvent(Asset asset) {
        this.asset = asset;
    }

    public Asset getAsset() {
        return asset;
    }
}
