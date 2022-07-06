package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.SISTE_PUBLISERTE_KVARTAL;

import io.vavr.control.Either;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.springframework.stereotype.Service;

@Service
public class AggregertHistorikkService {

    private final SykefraværRepository sykefraværprosentRepository;
    private final BransjeEllerNæringService bransjeEllerNæringService;
    private final TilgangskontrollService tilgangskontrollService;
    private final EnhetsregisteretClient enhetsregisteretClient;

    public AggregertHistorikkService(
            SykefraværRepository sykefraværprosentRepository,
            BransjeEllerNæringService bransjeEllerNæringService,
            TilgangskontrollService tilgangskontrollService,
            EnhetsregisteretClient enhetsregisteretClient) {
        this.sykefraværprosentRepository = sykefraværprosentRepository;
        this.bransjeEllerNæringService = bransjeEllerNæringService;
        this.tilgangskontrollService = tilgangskontrollService;
        this.enhetsregisteretClient = enhetsregisteretClient;
    }

    public Either<TilgangskontrollException, List<AggregertHistorikkDto>> hentAggregertHistorikk(
            Orgnr orgnr) {

        if (!tilgangskontrollService.brukerRepresentererVirksomheten(orgnr)) {
            return Either.left(
                    new TilgangskontrollException("Bruker mangler tilgang til denne virksomheten"));
        }

        Underenhet virksomhet = enhetsregisteretClient.hentUnderenhet(orgnr);
        if (!tilgangskontrollService.brukerHarIaRettigheter(orgnr)) {
            // return hentStatistikkUtenTallFraVirksomheten(virksomhet);
        }

        return Either.right(hentOgBearbeidStatistikk(virksomhet));
    }

    List<AggregertHistorikkDto> hentOgBearbeidStatistikk(Underenhet virksomhet) {
        Historikkdata sykefraværsdata =
                hentForSisteFemKvartaler(virksomhet);

        BransjeEllerNæring bransjeEllerNæring =
                bransjeEllerNæringService.skalHenteDataPåBransjeEllerNæringsnivå(
                        virksomhet.getNæringskode());

        Prosentkalkulator kalkulator = new Prosentkalkulator(sykefraværsdata);

        return Stream.of(
                        kalkulator.fraværsprosentVirksomhet(virksomhet.getNavn()),
                        kalkulator.fraværsprosentBransjeEllerNæring(bransjeEllerNæring),
                        kalkulator.fraværsprosentNorge(),
                        kalkulator.trendBransjeEllerNæring(bransjeEllerNæring))
                .filter(Either::isRight)
                .map(Either::get)
                .collect(Collectors.toList());
    }

    private Historikkdata
    hentForSisteFemKvartaler(Underenhet forBedrift) {
        return sykefraværprosentRepository
                .hentHistorikkAlleKategorier(forBedrift,
                        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(4));
    }
}

