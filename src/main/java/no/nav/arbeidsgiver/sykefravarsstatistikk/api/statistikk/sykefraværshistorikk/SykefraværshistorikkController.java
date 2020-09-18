package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.OverordnetEnhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværshistorikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværshistorikkService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SummertKorttidsOgLangtidsfravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.VarighetService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.InnloggetBruker;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Protected
@RestController
public class SykefraværshistorikkController {
    private final KvartalsvisSykefraværshistorikkService kvartalsvisSykefraværshistorikkService;
    private final TilgangskontrollService tilgangskontrollService;
    private final EnhetsregisteretClient enhetsregisteretClient;
    private final VarighetService varighetService;

    public SykefraværshistorikkController(
            KvartalsvisSykefraværshistorikkService kvartalsvisSykefraværshistorikkService,
            TilgangskontrollService tilgangskontrollService,
            EnhetsregisteretClient enhetsregisteretClient,
            VarighetService varighetService) {
        this.kvartalsvisSykefraværshistorikkService = kvartalsvisSykefraværshistorikkService;
        this.tilgangskontrollService = tilgangskontrollService;
        this.enhetsregisteretClient = enhetsregisteretClient;
        this.varighetService = varighetService;
    }

    @GetMapping(value = "/{orgnr}/sykefravarshistorikk/kvartalsvis")
    public List<KvartalsvisSykefraværshistorikk> hentSykefraværshistorikk(
            @PathVariable("orgnr") String orgnrStr,
            HttpServletRequest request
    ) {

        Orgnr orgnr = new Orgnr(orgnrStr);

        InnloggetBruker bruker = tilgangskontrollService.hentInnloggetBruker();

        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                orgnr,
                bruker,
                request.getMethod(),
                "" + request.getRequestURL()
        );

        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);
        OverordnetEnhet overordnetEnhet = enhetsregisteretClient.hentInformasjonOmEnhet(underenhet.getOverordnetEnhetOrgnr());

        boolean harTilgangTilOverordnetEnhet = tilgangskontrollService.hentTilgangTilOverordnetEnhetOgLoggSikkerhetshendelse(
                bruker,
                overordnetEnhet,
                underenhet,
                request.getMethod(),
                "" + request.getRequestURL()
        );

        if (harTilgangTilOverordnetEnhet) {
            return kvartalsvisSykefraværshistorikkService.hentSykefraværshistorikk(
                    underenhet,
                    overordnetEnhet
            );
        } else {
            return kvartalsvisSykefraværshistorikkService.hentSykefraværshistorikk(
                    underenhet,
                    overordnetEnhet.getInstitusjonellSektorkode()
            );
        }
    }


    @GetMapping(value = "/{orgnr}/sykefravarshistorikk/summert")
    public SummertKorttidsOgLangtidsfravær hentVarighet(
            @PathVariable("orgnr") String orgnrStr,
            @RequestParam("antallKvartaler") int antallKvartaler,
            HttpServletRequest request
    ) {
        Orgnr orgnr = new Orgnr(orgnrStr);

        InnloggetBruker bruker = tilgangskontrollService.hentInnloggetBruker();
        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                orgnr,
                bruker,
                request.getMethod(),
                "" + request.getRequestURL()
        );
        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);

        if (antallKvartaler != 4) {
            throw new IllegalArgumentException("For øyeblikket støtter vi kun summering av 4 kvartaler.");
        }

        return varighetService.hentSummertKorttidsOgLangtidsfravær(
                underenhet,
                new ÅrstallOgKvartal(2020, 2),
                antallKvartaler
        );
    }

    // TODO Disse er bare her for bakoverkompatilibitet med frontend; fjern når frontend er oppdatert
    @Deprecated
    @GetMapping(value = "/{orgnr}/varighetsiste4kvartaler")
    public SummertKorttidsOgLangtidsfravær hentVarighetOld(
            @PathVariable("orgnr") String orgnrStr,
            HttpServletRequest request
    ) {
        return hentVarighet(orgnrStr, 4, request);
    }

    @Deprecated
    @GetMapping(value = "/{orgnr}/sykefravarshistorikk")
    public List<KvartalsvisSykefraværshistorikk> hentSykefraværshistorikkOld(
            @PathVariable("orgnr") String orgnrStr,
            HttpServletRequest request
    ) {
        return hentSykefraværshistorikk(orgnrStr, request);
    }
}
