package de.rfelgent.tus.event;

import de.rfelgent.tus.domain.Asset;

/**
 * @author rfelgentraeger
 */
public class AssetTerminatedEvent extends AbstractAssetEvent {

    public AssetTerminatedEvent(Asset asset) {
        super(asset);
    }
}
