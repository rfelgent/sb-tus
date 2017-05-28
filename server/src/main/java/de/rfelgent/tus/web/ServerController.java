package de.rfelgent.tus.web;

import de.rfelgent.tus.Extensions;
import de.rfelgent.tus.Headers;
import de.rfelgent.tus.Version;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author rfelgentraeger
 */
@RestController
public class ServerController {

    @RequestMapping(value = {"", "/"}, method = {RequestMethod.OPTIONS})
    public ResponseEntity<Void> status() {
        return ResponseEntity.noContent()
                .header(Headers.TUS_VERSION, Version.SEMVERSION_1_0_0)
                .header(Headers.TUS_EXTENSION, Extensions.CREATION + "," + Extensions.EXPIRATION + "," + Extensions.TERMINATION)
                .header(Headers.TUS_MAXSIZE, "1073741824")  // 1GB
                .build();
    }
}
