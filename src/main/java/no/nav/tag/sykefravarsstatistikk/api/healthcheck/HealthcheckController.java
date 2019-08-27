package no.nav.tag.sykefravarsstatistikk.api.healthcheck;

import no.nav.security.oidc.api.Unprotected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Unprotected
@RestController
public class HealthcheckController {


    @GetMapping("/internal/healthcheck")
    public String healthcheck() {
        return "ok";
    }
}
