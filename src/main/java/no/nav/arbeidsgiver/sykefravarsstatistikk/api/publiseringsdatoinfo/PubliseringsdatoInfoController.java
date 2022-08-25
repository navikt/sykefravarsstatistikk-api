package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoinfo;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


@Slf4j
@Protected
@RestController
public class PubliseringsdatoInfoController {

    private final TilgangskontrollService tilgangskontrollService;
    private final PubliseringsdatoInfoService publiseringsdatoInfoService;


    public PubliseringsdatoInfoController(
            TilgangskontrollService tilgangskontrollService,
            PubliseringsdatoInfoService publiseringsdatoInfoService
    ) {
        this.tilgangskontrollService = tilgangskontrollService;
        this.publiseringsdatoInfoService = publiseringsdatoInfoService;
    }


    @GetMapping(value = "/publiseringsdato")
    public PubliseringsdatoInfo hentPubliseringsdatoInfo(
            HttpServletRequest request
    ) {
        return publiseringsdatoInfoService.hentPubliseringsdatoInfo();
    }

}
