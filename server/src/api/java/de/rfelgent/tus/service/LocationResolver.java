package de.rfelgent.tus.service;

import de.rfelgent.tus.domain.Asset;

/**
 * @author rfelgentraeger
 */
public interface LocationResolver {

    /**
     * @param asset
     * @return a relative or absolute URL
     */
    String resolve(Asset asset);
}
