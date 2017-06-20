package de.rfelgent.tus.event;

import de.rfelgent.tus.domain.Asset;

import java.net.URL;

/**
 * @author rfelgentraeger
 */
public class AssetCreatedEvent {

    private Asset asset;

    private String location;

    public AssetCreatedEvent(Asset asset, String location) {
        this.asset = asset;
        this.location = location;
    }

    public Asset getAsset() {
        return asset;
    }

    public String getLocation() {
        return location;
    }
}
