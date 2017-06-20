package de.rfelgent.tus.service;

import de.rfelgent.tus.domain.Asset;

/**
 * @author rfelgentraeger
 */
public interface IdGenerator {

    String generateId(Asset asset);
}
