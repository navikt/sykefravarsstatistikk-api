package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.SISTE_PUBLISERTE_KVARTAL;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.OverordnetEnhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.IngenNæringException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.AggregertStatistikkDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.AggregertStatistikkService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværshistorikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværshistorikkService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SummertLegemeldtSykefraværService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SummertSykefraværService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SummertSykefraværshistorikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.InnloggetBruker;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@Protected
@RestController
public class SykefraværshistorikkController {

    private final KvartalsvisSykefraværshistorikkService kvartalsvisSykefraværshistorikkService;
    private final TilgangskontrollService tilgangskontrollService;
    private final EnhetsregisteretClient enhetsregisteretClient;
    private final SummertSykefraværService summertSykefraværService;
    private final AggregertStatistikkService aggregertHistorikkService;

    //TODO: Fjern når "aggregert"-endepunktet har blitt tatt i bruk
    private final SummertLegemeldtSykefraværService summertLegemeldtSykefraværService;


    public SykefraværshistorikkController(
            KvartalsvisSykefraværshistorikkService kvartalsvisSykefraværshistorikkService,
            TilgangskontrollService tilgangskontrollService,
            EnhetsregisteretClient enhetsregisteretClient,
            SummertSykefraværService summertSykefraværService,
            AggregertStatistikkService aggregertHistorikkService,
            SummertLegemeldtSykefraværService summertLegemeldtSykefraværService
    ) {
        this.kvartalsvisSykefraværshistorikkService = kvartalsvisSykefraværshistorikkService;
        this.tilgangskontrollService = tilgangskontrollService;
        this.enhetsregisteretClient = enhetsregisteretClient;
        this.summertSykefraværService = summertSykefraværService;
        this.aggregertHistorikkService = aggregertHistorikkService;
        this.summertLegemeldtSykefraværService = summertLegemeldtSykefraværService;
    }


    @GetMapping(value = "/{orgnr}/sykefravarshistorikk/kvartalsvis")
    public List<KvartalsvisSykefraværshistorikk> hentSykefraværshistorikk(
            @PathVariable("orgnr") String orgnrStr,
            HttpServletRequest request
    ) {

        Orgnr orgnr = new Orgnr(orgnrStr);

        InnloggetBruker bruker = tilgangskontrollService.hentBrukerKunIaRettigheter();

        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                orgnr,
                bruker,
                request.getMethod(),
                "" + request.getRequestURL()
        );

        Underenhet underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);
        OverordnetEnhet overordnetEnhet = enhetsregisteretClient.hentInformasjonOmEnhet(
                underenhet.getOverordnetEnhetOrgnr());

        boolean harTilgangTilOverordnetEnhet =
                tilgangskontrollService.hentTilgangTilOverordnetEnhetOgLoggSikkerhetshendelse(
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

        InnloggetBruker bruker = tilgangskontrollService.hentBrukerKunIaRettigheter();
        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                new Orgnr(orgnrStr),
                bruker,
                request.getMethod(),
                "" + request.getRequestURL()
        );
        Underenhet underenhet
                = enhetsregisteretClient.hentInformasjonOmUnderenhet(new Orgnr(orgnrStr));

        if (antallKvartaler != 4) {
            throw new IllegalArgumentException(
                    "For øyeblikket støtter vi kun summering av 4 kvartaler.");
        }

        SummertSykefraværshistorikk summertSykefraværshistorikkVirksomhet =
                summertSykefraværService.hentSummertSykefraværshistorikk(
                        underenhet,
                        SISTE_PUBLISERTE_KVARTAL,
                        antallKvartaler
                );

        SummertSykefraværshistorikk summertSykefraværshistorikkBransjeEllerNæring =
                summertSykefraværService.hentSummertSykefraværshistorikkForBransjeEllerNæring(
                        underenhet,
                        SISTE_PUBLISERTE_KVARTAL,
                        antallKvartaler
                );

        return Arrays.asList(summertSykefraværshistorikkVirksomhet,
                summertSykefraværshistorikkBransjeEllerNæring);
    }


    // TODO: Fjern har vi har gått over til "aggregert"-endepunktet
    @GetMapping(value = "/{orgnr}/sykefravarshistorikk/legemeldtsykefravarsprosent")
    public ResponseEntity<LegemeldtSykefraværsprosent> hentLegemeldtSykefraværsprosent(
            @PathVariable("orgnr") String orgnrStr,
            HttpServletRequest request
    ) {
        InnloggetBruker bruker = tilgangskontrollService.hentBrukerKunIaRettigheter();

        tilgangskontrollService.sjekkTilgangTilOrgnrOgLoggSikkerhetshendelse(
                new Orgnr(orgnrStr),
                bruker,
                request.getMethod(),
                "" + request.getRequestURL()
        );

        Underenhet underenhet;
        try {
            underenhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(new Orgnr(orgnrStr));
        } catch (IngenNæringException e) {
            log.info("Underenhet har ingen næring. Returnerer 204 - No Content");
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .body(null);
        }

        LegemeldtSykefraværsprosent legemeldtSykefraværsprosent =
                summertLegemeldtSykefraværService.hentLegemeldtSykefraværsprosent(
                        underenhet,
                        SISTE_PUBLISERTE_KVARTAL
                );

        if (legemeldtSykefraværsprosent.getProsent() == null) {
            log.info(
                    "Underenhet har ingen sykefraværsprosent tilgjengelig. Returnerer 204 - No "
                            + "Content");
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT)
                    .body(null);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(legemeldtSykefraværsprosent);
    }


    @GetMapping("/{orgnr}/v1/sykefravarshistorikk/aggregert")
    public ResponseEntity<AggregertStatistikkDto> hentAggregertStatistikk(
            @PathVariable("orgnr") String orgnr
    ) {

        AggregertStatistikkDto statistikk =
                aggregertHistorikkService.hentAggregertStatistikk(new Orgnr(orgnr))
                        .getOrElseThrow(e -> e);

        return ResponseEntity.status(HttpStatus.OK).body(statistikk);
    }
}
