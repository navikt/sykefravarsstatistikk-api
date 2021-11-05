package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.OverordnetEnhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværshistorikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværshistorikkService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SummertLegemeldtSykefraværService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SummertSykefraværService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SummertSykefraværshistorikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.InnloggetBruker;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Protected
@RestController
public class SykefraværshistorikkController {
    private final KvartalsvisSykefraværshistorikkService kvartalsvisSykefraværshistorikkService;
    private final TilgangskontrollService tilgangskontrollService;
    private final EnhetsregisteretClient enhetsregisteretClient;
    private final SummertSykefraværService summertSykefraværService;
    private final SummertLegemeldtSykefraværService summertLegemeldtSykefraværService;

    public static final ÅrstallOgKvartal SISTE_PUBLISERTE_ÅRSTALL_OG_KVARTAL = new ÅrstallOgKvartal(2021, 2);


    public SykefraværshistorikkController(
            KvartalsvisSykefraværshistorikkService kvartalsvisSykefraværshistorikkService,
            TilgangskontrollService tilgangskontrollService,
            EnhetsregisteretClient enhetsregisteretClient,
            SummertSykefraværService summertSykefraværService,
            SummertLegemeldtSykefraværService summertLegemeldtSykefraværService
    ) {
        this.kvartalsvisSykefraværshistorikkService = kvartalsvisSykefraværshistorikkService;
        this.tilgangskontrollService = tilgangskontrollService;
        this.enhetsregisteretClient = enhetsregisteretClient;
        this.summertSykefraværService = summertSykefraværService;
        this.summertLegemeldtSykefraværService = summertLegemeldtSykefraværService;
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
    public List<SummertSykefraværshistorikk> hentSummertKorttidsOgLangtidsfraværV2(
            @PathVariable("orgnr") String orgnrStr,
            @RequestParam("antallKvartaler") int antallKvartaler,
            HttpServletRequest request
    ) {

        InnloggetBruker bruker = tilgangskontrollService.hentInnloggetBruker();
        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                new Orgnr(orgnrStr),
                bruker,
                request.getMethod(),
                "" + request.getRequestURL()
        );
        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(new Orgnr(orgnrStr));

        if (antallKvartaler != 4) {
            throw new IllegalArgumentException("For øyeblikket støtter vi kun summering av 4 kvartaler.");
        }

        SummertSykefraværshistorikk summertSykefraværshistorikkVirksomhet =
                summertSykefraværService.hentSummertSykefraværshistorikk(
                        underenhet,
                        SISTE_PUBLISERTE_ÅRSTALL_OG_KVARTAL,
                        antallKvartaler
                );

        SummertSykefraværshistorikk summertSykefraværshistorikkBransjeEllerNæring =
                summertSykefraværService.hentSummertSykefraværshistorikkForBransjeEllerNæring(
                        underenhet,
                        SISTE_PUBLISERTE_ÅRSTALL_OG_KVARTAL,
                        antallKvartaler
                );

        return Arrays.asList(summertSykefraværshistorikkVirksomhet, summertSykefraværshistorikkBransjeEllerNæring);
    }

    @GetMapping(value = "/{orgnr}/sykefravarshistorikk/legemeldtsykefravarsprosent")
    public LegemeldtSykefraværsprosent hentLegemeldtSykefraværsprosent(
            @PathVariable("orgnr") String orgnrStr,
            HttpServletRequest request
    ) {
        InnloggetBruker bruker = tilgangskontrollService.hentInnloggetBruker();

        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                new Orgnr(orgnrStr),
                bruker,
                request.getMethod(),
                "" + request.getRequestURL()
        );
        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(new Orgnr(orgnrStr));

        LegemeldtSykefraværsprosent legemeldtSykefraværsprosent =
                summertLegemeldtSykefraværService.hentLegemeldtSykefraværsprosent(
                        underenhet,
                        SISTE_PUBLISERTE_ÅRSTALL_OG_KVARTAL
                );

        // TODO: null er ikke robust nok
        // LegemeldtSykefraværsprosent ---> til klient app (= front-end)
        // eller trenger vi ett nytt/annet objekt?

        if (legemeldtSykefraværsprosent.getProsent() == null) {

        }
        return legemeldtSykefraværsprosent;
    }
}
