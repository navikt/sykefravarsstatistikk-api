package no.nav.tag.sykefravarsstatistikk.api;

import no.nav.security.oidc.api.Unprotected;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.EnhetsregisteretClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Unprotected
@RestController
public class TestController {

    private final EnhetsregisteretClient enhetsregisteretClient;

    public TestController(EnhetsregisteretClient enhetsregisteretClient) {
        this.enhetsregisteretClient = enhetsregisteretClient;
    }

    @GetMapping(value = "/test/{orgnr}")
    public Underenhet test(
            @PathVariable("orgnr") String orgnr
    ) {
        return enhetsregisteretClient.hentInformasjonOmUnderenhet(new Orgnr(orgnr));
    }
}
