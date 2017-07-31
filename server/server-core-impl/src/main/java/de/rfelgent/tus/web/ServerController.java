package de.rfelgent.tus.web;

import de.rfelgent.tus.TusExtensions;
import de.rfelgent.tus.TusHeaders;
import de.rfelgent.tus.TusVersion;
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
                .header(TusHeaders.TUS_VERSION, TusVersion.SEMVERSION_1_0_0)
                .header(TusHeaders.TUS_EXTENSION, TusExtensions.CREATION + "," + TusExtensions.CREATION_DEFER
                    + "," + TusExtensions.EXPIRATION + "," + TusExtensions.TERMINATION)
                .header(TusHeaders.TUS_MAXSIZE, "1073741824")  // 1GB
                .build();
    }
}
