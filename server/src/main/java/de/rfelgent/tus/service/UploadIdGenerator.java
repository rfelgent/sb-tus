package de.rfelgent.tus.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author rfelgentraeger
 */
@Service
public class UploadIdGenerator {

    public String generateId() {
        return UUID.randomUUID().toString();
    }
}
