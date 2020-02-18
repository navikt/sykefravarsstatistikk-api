package no.nav.tag.sykefravarsstatistikk.api.tapteDagsverkForKostnadsberegning;

import no.nav.security.oidc.api.Protected;
import no.nav.tag.sykefravarsstatistikk.api.domene.InnloggetBruker;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@Protected
public class TapteDagsverkForKostnadsberegningController {
    private final TapteDagsverkService service;
    private final TilgangskontrollService tilgangskontrollService;

    public TapteDagsverkForKostnadsberegningController(TapteDagsverkService service, TilgangskontrollService tilgangskontrollService) {
        this.service = service;
        this.tilgangskontrollService = tilgangskontrollService;
    }

    @GetMapping(value = "/{orgnr}/summerTapteDagsverk")
    public TapteDagsverk summerTapteDagsverk(
            @PathVariable("orgnr") String orgnrStr,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        Orgnr orgnr = new Orgnr(orgnrStr);
        InnloggetBruker innloggetBruker = tilgangskontrollService.hentInnloggetBruker();
        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                orgnr,
                innloggetBruker,
                request.getMethod(),
                "" + request.getRequestURL()
        );

        return service.hentSummerTapteDagsverk(orgnr);
    }

    @GetMapping(value = "/{orgnr}/tapteDagsverk")
    public List<KvartalsvisTapteDagsverk> tapteDagsverk(
            @PathVariable("orgnr") String orgnrStr,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        Orgnr orgnr = new Orgnr(orgnrStr);
        InnloggetBruker innloggetBruker = tilgangskontrollService.hentInnloggetBruker();
        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                orgnr,
                innloggetBruker,
                request.getMethod(),
                "" + request.getRequestURL()
        );

        return service.hentTapteDagsverkFraDeSiste4Kvartalene(orgnr);
    }
}
