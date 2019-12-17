package no.nav.tag.sykefravarsstatistikk.api.metrikker;

import no.nav.metrics.MetricsFactory;
import no.nav.security.oidc.api.Unprotected;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Unprotected
@RestController
public class MetrikkController {

    @PostMapping("/metrikker/{eventName}")
    public ResponseEntity sendEvent(
            @PathVariable("eventName") String eventName
    ) {
        MetricsFactory.createEvent(eventName).report();
        return ResponseEntity.ok().build();
    }

}
