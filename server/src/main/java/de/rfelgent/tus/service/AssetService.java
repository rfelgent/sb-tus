package de.rfelgent.tus.service;

import de.rfelgent.tus.domain.Asset;
import org.springframework.stereotype.Service;

/**
 * @author rfelgentraeger
 */
@Service
public class AssetService {

    public boolean existsAsset(String id) {
        return false;
    }

    public Asset findAsset(String id) {
        return new Asset();
    }
}
