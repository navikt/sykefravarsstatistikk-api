package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


@Slf4j
@Protected
@RestController
public class PubliseringsdatoerController {

    private final TilgangskontrollService tilgangskontrollService;
    private final PubliseringsdatoerService publiseringsdatoerService;


    public PubliseringsdatoerController(
            TilgangskontrollService tilgangskontrollService,
            PubliseringsdatoerService publiseringsdatoerService
    ) {
        this.tilgangskontrollService = tilgangskontrollService;
        this.publiseringsdatoerService = publiseringsdatoerService;
    }


    @GetMapping(value = "/publiseringsdato")
    public Publiseringsdatoer hentPubliseringsdatoInfo(
            HttpServletRequest request
    ) {
        return publiseringsdatoerService.hentPubliseringsdatoer();
    }

}
