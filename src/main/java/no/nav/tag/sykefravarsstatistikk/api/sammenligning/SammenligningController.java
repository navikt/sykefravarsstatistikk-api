package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import no.nav.metrics.MetricsFactory;
import no.nav.metrics.Timer;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.sykefravarsstatistikk.api.domene.InnloggetBruker;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static no.nav.tag.sykefravarsstatistikk.api.sammenligning.CookieUtils.hentCookieEllerGenererNy;

@Protected
@RestController
public class SammenligningController {

    private final SammenligningService service;
    private final TilgangskontrollService tilgangskontrollService;

    private final static String SESSION_ID_COOKIE_NAME = "sykefravarsstatistikk-session";

    @Autowired
    public SammenligningController(SammenligningService service, TilgangskontrollService tilgangskontrollService) {
        this.service = service;
        this.tilgangskontrollService = tilgangskontrollService;
    }

    @GetMapping(value = "/{orgnr}/sammenligning")
    public Sammenligning sammenligning(
            @PathVariable("orgnr") String orgnrStr,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Timer timer = MetricsFactory.createTimer("sykefravarsstatistikk.sammenligning").start();

        Orgnr orgnr = new Orgnr(orgnrStr);
        InnloggetBruker innloggetSelvbetjeningBruker = tilgangskontrollService.hentInnloggetBruker();
        utførTilgangskontroll(orgnr, innloggetSelvbetjeningBruker, request);
        String sessionId = hentCookieEllerGenererNy(request, response, SESSION_ID_COOKIE_NAME);
        Sammenligning sammenligning = service.hentSammenligningForUnderenhet(
                orgnr,
                innloggetSelvbetjeningBruker,
                sessionId
        );

        timer.stop().report();

        return sammenligning;
    }

    private void utførTilgangskontroll(Orgnr orgnr, InnloggetBruker bruker, HttpServletRequest request) {
        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                orgnr,
                bruker,
                request.getMethod(),
                "" + request.getRequestURL()
        );
    }

}
