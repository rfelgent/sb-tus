package de.rfelgent.tus.event;

import de.rfelgent.tus.domain.Asset;

import java.net.URL;

/**
 * @author rfelgentraeger
 */
public class AssetCreatedEvent {

    private Asset asset;

    private URL location;

    public AssetCreatedEvent(Asset asset, URL location) {
        this.asset = asset;
        this.location = location;
    }

    public Asset getAsset() {
        return asset;
    }

    public URL getLocation() {
        return location;
    }
}
