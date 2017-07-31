package de.rfelgent.tus.event;

import de.rfelgent.tus.domain.Asset;

import java.net.URL;

/**
 * @author rfelgentraeger
 */
public class AssetEventCreated extends AbstractAssetEvent {

    private String location;

    public AssetEventCreated(Asset asset, String location) {
        super(asset);
        this.location = location;
    }

    public String getLocation() {
        return location;
    }
}
