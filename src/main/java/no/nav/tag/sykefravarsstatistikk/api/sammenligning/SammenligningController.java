package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Protected
@RestController
@Slf4j
public class SammenligningController {

    private final SammenligningService service;
    private final TilgangskontrollService tilgangskontrollService;

    private final static String STATISTIKK_ID_COOKIE_NAME = "statistikk-id";

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
        Orgnr orgnr = new Orgnr(orgnrStr);
        utførTilgangskontroll(orgnr, request);
        String statistikkId = settPåCookieOgReturnerStatistikkId(request, response);
        return service.hentSammenligningForUnderenhet(orgnr, statistikkId);
    }

    private void utførTilgangskontroll(Orgnr orgnr, HttpServletRequest request) {
        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                orgnr,
                request.getMethod(),
                "" + request.getRequestURL()
        );
    }

    private String settPåCookieOgReturnerStatistikkId(HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie> cookie = getStatistikkIdCookie(request);

        if (cookie.isPresent()) {
            return cookie.get().getValue();
        } else {
            return setStatistikkIdCookie(response);
        }
    }

    private Optional<Cookie> getStatistikkIdCookie(HttpServletRequest request) {
        return Arrays.stream(request.getCookies())
                .filter(cookie -> STATISTIKK_ID_COOKIE_NAME.equals(cookie.getName()))
                .findAny();
    }

    private String setStatistikkIdCookie(HttpServletResponse response) {
        String statistikkId = UUID.randomUUID().toString();
        response.addCookie(new Cookie(STATISTIKK_ID_COOKIE_NAME, statistikkId));
        return statistikkId;
    }

}
