package de.rfelgent.tus.event;

import de.rfelgent.tus.domain.Asset;

import java.net.URL;

/**
 * @author rfelgentraeger
 */
public class AssetCreatedEvent extends AbstractAssetEvent {

    private String location;

    public AssetCreatedEvent(Asset asset, String location) {
        super(asset);
        this.location = location;
    }

    public String getLocation() {
        return location;
    }
}
