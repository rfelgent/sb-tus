package de.rfelgent.tus.service;

import de.rfelgent.tus.domain.Asset;
import java.util.UUID;

/**
 * @author rfelgentraeger
 */
public class IdGeneratorUuid implements IdGenerator {

    @Override
    public String generateId(Asset asset) {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
