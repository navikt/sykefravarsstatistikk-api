package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_DOWN;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.utils.CollectionUtils.joinLists;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.SISTE_PUBLISERTE_KVARTAL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.sisteFireKvartaler;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.LAND;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.VIRKSOMHET;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal.hentUtKvartal;

import io.vavr.control.Either;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.oppsummert.OppsummertStatistikkDto;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class OppsummertSykefravarsstatistikkService {

    private final SykefraværRepository sykefraværprosentRepository;
    private final BransjeEllerNæringService bransjeEllerNæringService;
    private final TilgangskontrollService tilgangskontrollService;
    private final EnhetsregisteretClient enhetsregisteretClient;

    public OppsummertSykefravarsstatistikkService(
          SykefraværRepository sykefraværprosentRepository,
          BransjeEllerNæringService bransjeEllerNæringService,
          TilgangskontrollService tilgangskontrollService,
          EnhetsregisteretClient enhetsregisteretClient) {
        this.sykefraværprosentRepository = sykefraværprosentRepository;
        this.bransjeEllerNæringService = bransjeEllerNæringService;
        this.tilgangskontrollService = tilgangskontrollService;
        this.enhetsregisteretClient = enhetsregisteretClient;
    }

    public Either<TilgangskontrollException, List<OppsummertStatistikkDto>> hentOppsummertStatistikk(
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

    List<OppsummertStatistikkDto> hentOgBearbeidStatistikk(Underenhet virksomhet) {
        Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> sykefraværsdata =
              hentUmaskertStatistikkForSisteFemKvartaler(virksomhet);

        BransjeEllerNæring bransjeEllerNæring =
              bransjeEllerNæringService.skalHenteDataPåBransjeEllerNæringsnivå(
                    virksomhet.getNæringskode());

        return Stream.of(
                    sykefraværVirksomhet(
                          virksomhet, sykefraværsdata),
                    fraværsprosentBransjeEllerNæring(
                          sykefraværsdata, bransjeEllerNæring),
                    fraværsprosentNorge(
                          sykefraværsdata),
                    trendBransjeEllerNæring(
                          sykefraværsdata, bransjeEllerNæring))
              .filter(Either::isRight)
              .map(Either::get)
              .collect(Collectors.toList());
    }

    private Either<ManglendeDataException, OppsummertStatistikkDto> trendBransjeEllerNæring(
          Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> sykefraværsdata,
          BransjeEllerNæring bransjeEllerNæring) {
        Either<ManglendeDataException, Trend> maybeTrend = Trend.kalkulerTrend(
              sykefraværsdata.get(bransjeEllerNæring.getTrendkategori()));

        return maybeTrend.map(r -> r.tilOppsummertStatistikkDto(
              bransjeEllerNæring.getTrendkategori(),
              bransjeEllerNæring.getVerdiSomString())
        );
    }

    private Either<ManglendeDataException, OppsummertStatistikkDto> fraværsprosentNorge(
          Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> sykefraværsdata) {
        return hentSummerbartSykefravær(sykefraværsdata.get(LAND))
              .tilGenerellStatistikkDto(LAND, "Norge");
    }

    private Either<ManglendeDataException, OppsummertStatistikkDto> fraværsprosentBransjeEllerNæring(
          Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> sykefraværsdata,
          BransjeEllerNæring bransjeEllerNæring) {
        return hentSummerbartSykefravær(
              sykefraværsdata.get(bransjeEllerNæring.getStatistikkategori()))
              .tilGenerellStatistikkDto(
                    bransjeEllerNæring.getStatistikkategori(),
                    bransjeEllerNæring.getVerdiSomString());
    }

    private Either<ManglendeDataException, OppsummertStatistikkDto> sykefraværVirksomhet(
          Underenhet virksomhet,
          Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> sykefraværsdata) {
        return hentSummerbartSykefravær(sykefraværsdata.get(VIRKSOMHET))
              .tilGenerellStatistikkDto(VIRKSOMHET, virksomhet.getNavn());
    }


    private Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>>
    hentUmaskertStatistikkForSisteFemKvartaler(Underenhet forBedrift) {
        ÅrstallOgKvartal fraKvartal = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(4);
        return sykefraværprosentRepository
              .hentUmaskertSykefraværAlleKategorier(forBedrift, fraKvartal);
    }


    private SummerbartSykefravær hentSummerbartSykefravær(
          List<UmaskertSykefraværForEttKvartal> statistikk) {

        return ekstraherSisteFireKvartaler(statistikk).stream()
              .map(SummerbartSykefravær::new)
              .reduce(SummerbartSykefravær.NULLPUNKT, SummerbartSykefravær::leggSammen);
    }


    private List<UmaskertSykefraværForEttKvartal> ekstraherSisteFireKvartaler(
          List<UmaskertSykefraværForEttKvartal> statistikk) {
        if (statistikk == null) {
            return List.of();
        }
        return statistikk.stream()
              .filter(data -> sisteFireKvartaler().contains(data.getÅrstallOgKvartal()))
              .collect(Collectors.toList());
    }


    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    static
    class Trend {

        private static final ÅrstallOgKvartal nyesteKvartal = SISTE_PUBLISERTE_KVARTAL;
        private static final ÅrstallOgKvartal ettÅrTidligere =
              SISTE_PUBLISERTE_KVARTAL.minusEttÅr();
        public BigDecimal trendverdi;
        public int antallTilfellerIBeregningen;
        public List<ÅrstallOgKvartal> kvartalerIBeregningen;

        public static Either<ManglendeDataException, Trend> kalkulerTrend(
              List<UmaskertSykefraværForEttKvartal> sykefravær) {

            Optional<UmaskertSykefraværForEttKvartal> nyesteSykefravær =
                  hentUtKvartal(sykefravær, nyesteKvartal);
            Optional<UmaskertSykefraværForEttKvartal> sykefraværetEtÅrSiden =
                  hentUtKvartal(sykefravær, ettÅrTidligere);

            if (nyesteSykefravær.isEmpty() || sykefraværetEtÅrSiden.isEmpty()) {
                return Either.left(
                      new ManglendeDataException(
                            "Mangler " + nyesteKvartal + " eller " + ettÅrTidligere));
            }

            BigDecimal trendverdi = nyesteSykefravær.get().getProsent()
                  .subtract(sykefraværetEtÅrSiden.get().getProsent());
            int antallTilfeller =
                  nyesteSykefravær.get().antallPersoner
                        + sykefraværetEtÅrSiden.get().antallPersoner;

            return Either.right(
                  new Trend(
                        trendverdi,
                        antallTilfeller,
                        List.of(SISTE_PUBLISERTE_KVARTAL, SISTE_PUBLISERTE_KVARTAL.minusEttÅr())
                  ));
        }

        public OppsummertStatistikkDto tilOppsummertStatistikkDto(
              Statistikkategori type, String label) {
            return new OppsummertStatistikkDto(
                  type,
                  label,
                  this.trendverdi.toString(),
                  this.antallTilfellerIBeregningen,
                  this.kvartalerIBeregningen);
        }
    }
}

@ToString
@EqualsAndHashCode
@AllArgsConstructor
class SummerbartSykefravær {

    static SummerbartSykefravær NULLPUNKT = new SummerbartSykefravær(ZERO, ZERO, 0, List.of());

    public BigDecimal mulige;
    public BigDecimal tapte;
    public int antallTilfellerIGrunnlaget;
    public List<ÅrstallOgKvartal> kvartalerIGrunnlaget;

    SummerbartSykefravær(@NotNull UmaskertSykefraværForEttKvartal data) {
        this.mulige = data.muligeDagsverk;
        this.tapte = data.tapteDagsverk;
        this.antallTilfellerIGrunnlaget = data.antallPersoner;
        this.kvartalerIGrunnlaget = List.of(data.getÅrstallOgKvartal());
    }

    public Either<ManglendeDataException, OppsummertStatistikkDto> tilGenerellStatistikkDto(
          Statistikkategori type, String label) {

        return kalkulerFraværsprosent().map(prosent -> new OppsummertStatistikkDto(
              type,
              label,
              prosent.toString(),
              antallTilfellerIGrunnlaget,
              kvartalerIGrunnlaget));
    }

    Either<ManglendeDataException, BigDecimal> kalkulerFraværsprosent() {
        if (this.equals(NULLPUNKT)) {
            return Either.left(new ManglendeDataException("Ingen sykefraværsdata tilgjengelig."));
        }

        return Either.right(
              tapte.divide(mulige, 2, HALF_DOWN).multiply(new BigDecimal(100)));
    }

    SummerbartSykefravær leggSammen(@NotNull SummerbartSykefravær other) {
        return new SummerbartSykefravær(
              this.mulige.add(other.mulige),
              this.tapte.add(other.tapte),
              this.antallTilfellerIGrunnlaget + other.antallTilfellerIGrunnlaget,
              joinLists(this.kvartalerIGrunnlaget, other.kvartalerIGrunnlaget));
    }
}

