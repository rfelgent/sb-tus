package de.rfelgent.tus.event;

import de.rfelgent.tus.domain.Asset;

/**
 * @author rfelgentraeger
 */
public class AssetCreatedEvent {

    private Asset asset;

    public AssetCreatedEvent(Asset asset) {
        this.asset = asset;
    }

    public Asset getAsset() {
        return asset;
    }
}
