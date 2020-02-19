package no.nav.tag.sykefravarsstatistikk.api.sykefraværshistorikk;

import no.nav.security.oidc.api.Protected;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Protected
@RestController
public class SykefraværshistorikkController {
    private final SykefraværshistorikkService sykefraværshistorikkService;
    private final TilgangskontrollService tilgangskontrollService;

    public SykefraværshistorikkController(SykefraværshistorikkService sykefraværshistorikkService, TilgangskontrollService tilgangskontrollService) {
        this.sykefraværshistorikkService = sykefraværshistorikkService;
        this.tilgangskontrollService = tilgangskontrollService;
    }

    @GetMapping(value = "/{orgnr}/sykefravarshistorikk")
    public List<Sykefraværshistorikk> hentSykefraværshistorikk(
            @PathVariable("orgnr") String orgnrStr,
            HttpServletRequest request
    ) {

        Orgnr orgnr = new Orgnr(orgnrStr);

        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                orgnr,
                request.getMethod(),
                "" + request.getRequestURL()
        );

        return sykefraværshistorikkService.hentSykefraværshistorikk(orgnr);
    }
}
