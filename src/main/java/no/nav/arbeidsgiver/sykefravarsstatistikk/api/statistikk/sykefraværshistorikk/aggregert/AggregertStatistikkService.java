package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.SISTE_PUBLISERTE_KVARTAL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.VIRKSOMHET;

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

    public Either<TilgangskontrollException, List<AggregertStatistikkDto>> hentAggregertStatistikk(
          Orgnr orgnr) {

        if (!tilgangskontrollService.brukerRepresentererVirksomheten(orgnr)) {
            return Either.left(
                  new TilgangskontrollException("Bruker mangler tilgang til denne virksomheten"));
        }

        Underenhet virksomhet = enhetsregisteretClient.hentUnderenhet(orgnr);
        Sykefraværsdata sykefraværsdata = hentForSisteFemKvartaler(virksomhet);

        if (!tilgangskontrollService.brukerHarIaRettigheter(orgnr)) {
            sykefraværsdata.filtrerBortKategori(VIRKSOMHET);
        }

        return Either.right(aggregerData(virksomhet, sykefraværsdata));
    }


    private List<AggregertStatistikkDto> aggregerData(
          Underenhet virksomhet,
          Sykefraværsdata sykefravær) {

        // TODO: rename
        Prosentkalkulator kalkulator = new Prosentkalkulator(sykefravær);

        BransjeEllerNæring bransjeEllerNæring =
              bransjeEllerNæringService.skalHenteDataPåBransjeEllerNæringsnivå(
                    virksomhet.getNæringskode());

        return Stream.of(
                    kalkulator.fraværsprosentVirksomhet(virksomhet.getNavn()),
                    kalkulator.fraværsprosentBransjeEllerNæring(bransjeEllerNæring),
                    kalkulator.fraværsprosentNorge(),
                    kalkulator.trendBransjeEllerNæring(bransjeEllerNæring))
              .filter(Either::isRight)
              .map(Either::get)
              .collect(Collectors.toList());
    }

    private Sykefraværsdata
    hentForSisteFemKvartaler(Underenhet forBedrift) {
        return sykefraværprosentRepository
                .hentUmaskertSykefraværAlleKategorier(forBedrift,
                        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(4));
    }
}

