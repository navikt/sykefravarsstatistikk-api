package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Protected
@RestController
@Slf4j
public class SammenligningController {

    private final SammenligningService service;
    private final TilgangskontrollService tilgangskontrollService;

    @Autowired
    public SammenligningController(SammenligningService service, TilgangskontrollService tilgangskontrollService){
        this.service = service;
        this.tilgangskontrollService = tilgangskontrollService;
    }

    @GetMapping(value = "/{orgnr}/sammenligning")
    public Sammenligning sammenligning(
            @PathVariable("orgnr") String orgnrStr,
            HttpServletRequest request
    ) {
        Orgnr orgnr = new Orgnr(orgnrStr);
        utførTilgangskontroll(orgnr, request);
        return service.hentSammenligningForUnderenhet(orgnr);
    }

    private void utførTilgangskontroll(Orgnr orgnr, HttpServletRequest request) {
        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                orgnr,
                request.getMethod(),
                "" + request.getRequestURL()
        );
    }

}
