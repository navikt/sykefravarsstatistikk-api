package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.healthcheck;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Unprotected
@RestController
public class HealthcheckController {

    private final EnhetsregisteretClient enhetsregisteretClient;

    public HealthcheckController(EnhetsregisteretClient enhetsregisteretClient) {
        this.enhetsregisteretClient = enhetsregisteretClient;
    }

    @GetMapping("/internal/healthcheck")
    public String healthcheck() {
        return "ok";
    }

    @GetMapping("/internal/healthcheck/avhengigheter")
    public Map<String, HttpStatus> sjekkAvhengigheter() {
        Map<String, HttpStatus> statuser = new HashMap<>();
        statuser.put("enhetsregisteret", enhetsregisteretClient.healthcheck());
        return statuser;
    }
}
