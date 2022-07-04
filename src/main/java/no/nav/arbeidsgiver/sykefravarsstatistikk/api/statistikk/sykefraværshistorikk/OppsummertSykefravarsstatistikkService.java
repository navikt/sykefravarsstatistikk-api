package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_DOWN;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Konstanter.SISTE_PUBLISERTE_KVARTAL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.utils.CollectionUtils.joinLists;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.sisteFireKvartaler;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.LAND;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.VIRKSOMHET;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal.hentUtKvartal;

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
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.InnloggetBruker;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.apache.commons.lang3.NotImplementedException;
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

    public List<OppsummertStatistikkDto> hentOppsummertStatistikk(String orgnr,
          InnloggetBruker bruker) {

        Underenhet virksomhet = enhetsregisteretClient.hentUnderenhet(new Orgnr(orgnr));

        // Har brukeren IA-rettigheter?
        return hentStatistikkUtenTallFraVirksomheten(virksomhet);

        // Brukeren HAR ia-rettigheter. Skal tallene maskeres?

        List<OppsummertStatistikkDto> statistikk = hentOgBearbeidStatistikk(virksomhet);


    }

    private List<OppsummertStatistikkDto> hentStatistikkUtenTallFraVirksomheten(
          Underenhet virksomhet) {
        // TODO
        throw new NotImplementedException();
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
              .filter(Optional::isPresent)
              .map(Optional::get)
              .collect(Collectors.toList());
    }

    private Optional<OppsummertStatistikkDto> trendBransjeEllerNæring(
          Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> sykefraværsdata,
          BransjeEllerNæring bransjeEllerNæring) {
        Optional<OppsummertStatistikkDto> trendverdiBransjeEllerNæring =
              Trend.kalkulerTrend(sykefraværsdata.get(bransjeEllerNæring.getTrendkategori()))
                    .tilOppsummertStatistikkDto(
                          bransjeEllerNæring.getTrendkategori(),
                          bransjeEllerNæring.getVerdiSomString());
        return trendverdiBransjeEllerNæring;
    }

    private Optional<OppsummertStatistikkDto> fraværsprosentNorge(
          Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> sykefraværsdata) {
        return hentSummerbartSykefravær(sykefraværsdata.get(LAND))
              .tilGenerellStatistikkDto(LAND, "Norge");
    }

    private Optional<OppsummertStatistikkDto> fraværsprosentBransjeEllerNæring(
          Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> sykefraværsdata,
          BransjeEllerNæring bransjeEllerNæring) {
        return hentSummerbartSykefravær(
              sykefraværsdata.get(bransjeEllerNæring.getStatistikkategori()))
              .tilGenerellStatistikkDto(
                    bransjeEllerNæring.getStatistikkategori(),
                    bransjeEllerNæring.getVerdiSomString());
    }

    private Optional<OppsummertStatistikkDto> sykefraværVirksomhet(
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

    public Optional<OppsummertStatistikkDto> tilGenerellStatistikkDto(
          Statistikkategori type, String label) {
        return this.erTom() ? Optional.empty() : Optional.of(
              new OppsummertStatistikkDto(
                    type,
                    label,
                    kalkulerFraværsprosent(),
                    antallTilfellerIGrunnlaget,
                    kvartalerIGrunnlaget)
        );
    }

    public boolean erTom() {
        return this.equals(SummerbartSykefravær.NULLPUNKT);
    }

    String kalkulerFraværsprosent() {
        return tapte.divide(mulige, 2, HALF_DOWN).multiply(new BigDecimal(100)).toString();
    }

    SummerbartSykefravær leggSammen(@NotNull SummerbartSykefravær other) {
        return new SummerbartSykefravær(
              this.mulige.add(other.mulige),
              this.tapte.add(other.tapte),
              this.antallTilfellerIGrunnlaget + other.antallTilfellerIGrunnlaget,
              joinLists(this.kvartalerIGrunnlaget, other.kvartalerIGrunnlaget));
    }
}


@ToString
@EqualsAndHashCode
@AllArgsConstructor
class Trend {

    static Trend NULLPUNKT = new Trend(ZERO, 0, List.of());
    public BigDecimal trendverdi;
    public int antallTilfellerIBeregningen;
    public List<ÅrstallOgKvartal> kvartalerIBeregningen;

    public static Trend kalkulerTrend(List<UmaskertSykefraværForEttKvartal> sykefravær) {

        Optional<UmaskertSykefraværForEttKvartal> maybeNyesteSykefravær =
              hentUtKvartal(sykefravær, SISTE_PUBLISERTE_KVARTAL);
        Optional<UmaskertSykefraværForEttKvartal> maybeSykefraværetEtÅrSiden =
              hentUtKvartal(sykefravær, SISTE_PUBLISERTE_KVARTAL.minusEttÅr());

        if (maybeNyesteSykefravær.isEmpty() || maybeSykefraværetEtÅrSiden.isEmpty()) {
            return Trend.NULLPUNKT;
        }

        UmaskertSykefraværForEttKvartal nyesteSykefravær = maybeNyesteSykefravær.get();
        UmaskertSykefraværForEttKvartal sykefraværetEttÅrSiden = maybeSykefraværetEtÅrSiden.get();

        BigDecimal trendverdi = nyesteSykefravær.getProsent()
              .subtract(sykefraværetEttÅrSiden.getProsent());
        int antallTilfeller =
              nyesteSykefravær.antallPersoner + sykefraværetEttÅrSiden.antallPersoner;

        return new Trend(
              trendverdi,
              antallTilfeller,
              List.of(SISTE_PUBLISERTE_KVARTAL, SISTE_PUBLISERTE_KVARTAL.minusEttÅr()));

    }

    public boolean erTom() {
        return this.equals(NULLPUNKT);
    }

    public Optional<OppsummertStatistikkDto> tilOppsummertStatistikkDto(Statistikkategori type,
          String label) {
        return this.erTom() ? Optional.empty() : Optional.of(new OppsummertStatistikkDto(
              type,
              label,
              this.trendverdi.toString(),
              this.antallTilfellerIBeregningen,
              this.kvartalerIBeregningen));
    }
}
