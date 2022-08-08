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
public class AggregertStatistikkService {

    private final SykefraværRepository sykefraværprosentRepository;
    private final BransjeEllerNæringService bransjeEllerNæringService;
    private final TilgangskontrollService tilgangskontrollService;
    private final EnhetsregisteretClient enhetsregisteretClient;

    public AggregertStatistikkService(
            SykefraværRepository sykefraværprosentRepository,
            BransjeEllerNæringService bransjeEllerNæringService,
            TilgangskontrollService tilgangskontrollService,
            EnhetsregisteretClient enhetsregisteretClient) {
        this.sykefraværprosentRepository = sykefraværprosentRepository;
        this.bransjeEllerNæringService = bransjeEllerNæringService;
        this.tilgangskontrollService = tilgangskontrollService;
        this.enhetsregisteretClient = enhetsregisteretClient;
    }

    public Either<TilgangskontrollException, AggregertStatistikkDto> hentAggregertStatistikk(
            Orgnr orgnr) {

        if (!tilgangskontrollService.brukerRepresentererVirksomheten(orgnr)) {
            return Either.left(
                    new TilgangskontrollException("Bruker mangler tilgang til denne virksomheten"));
        }

        Underenhet virksomhet = enhetsregisteretClient.hentInformasjonOmUnderenhet(orgnr);
        Sykefraværsdata sykefraværsdata = hentForSisteFemKvartaler(virksomhet);

        if (!tilgangskontrollService.brukerHarIaRettigheter(orgnr)) {
            sykefraværsdata.filtrerBortVirksomhetsdata();
        }

        return Either.right(aggregerData(virksomhet, sykefraværsdata));
    }


    private AggregertStatistikkDto aggregerData(Underenhet virksomhet, Sykefraværsdata sykefravær) {

        Aggregeringskalkulator kalkulator = new Aggregeringskalkulator(sykefravær);

        BransjeEllerNæring bransjeEllerNæring =
                bransjeEllerNæringService.skalHenteDataPåBransjeEllerNæringsnivå(
                        virksomhet.getNæringskode());

        List<StatistikkDto> prosentSisteFireKvartaler = Stream.of(
                        kalkulator.fraværsprosentVirksomhet(virksomhet.getNavn()),
                        kalkulator.fraværsprosentBransjeEllerNæring(bransjeEllerNæring),
                        kalkulator.fraværsprosentNorge())
                .filter(Either::isRight)
                .map(Either::get)
                .collect(Collectors.toList());

        List<StatistikkDto> trend = Stream.of(
                        kalkulator.trendBransjeEllerNæring(bransjeEllerNæring))
                .filter(Either::isRight)
                .map(Either::get)
                .collect(Collectors.toList());

        return new AggregertStatistikkDto(prosentSisteFireKvartaler, trend);

    }

    private Sykefraværsdata hentForSisteFemKvartaler(Underenhet forBedrift) {
        return sykefraværprosentRepository
                .hentUmaskertSykefraværAlleKategorier(forBedrift,
                        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(4));
    }
}

