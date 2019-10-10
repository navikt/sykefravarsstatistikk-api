package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Unprotected;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Unprotected
@RestController
@Slf4j
public class SammenligningController {

    private final SammenligningService service;
    private final TilgangskontrollService tilgangskontrollService;

    public static final String CORRELATION_ID = "correlationId";

    @Autowired
    public SammenligningController(SammenligningService service, TilgangskontrollService tilgangskontrollService){
        this.service = service;
        this.tilgangskontrollService = tilgangskontrollService;
    }

    @GetMapping(value = "/{orgnr}/sammenligning")
    public Sammenligning sammenligning(
            @PathVariable("orgnr") String orgnr,
            HttpServletRequest request
    ) {
        Sammenligning sammenligning;

        // TODO Best practice er å bruke MDC i en interceptor.
        try {
            MDC.put(CORRELATION_ID, UUID.randomUUID().toString());
            utførTilgangskontroll(orgnr, request);
            sammenligning = service.hentSammenligning();
        } finally {
            MDC.remove(CORRELATION_ID);
        }

        return sammenligning;
    }

    private void utførTilgangskontroll(String orgnr, HttpServletRequest request) {
        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                new Orgnr(orgnr),
                request.getMethod(),
                "" + request.getRequestURL()
        );
    }

}
