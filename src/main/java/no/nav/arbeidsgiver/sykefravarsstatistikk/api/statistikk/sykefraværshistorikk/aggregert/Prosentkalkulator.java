package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.sisteFireKvartaler;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.LAND;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.VIRKSOMHET;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert.SumAvSykefraværOverFlereKvartaler.NULLPUNKT;

import io.vavr.control.Either;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.StatistikkUtils;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.Agreggeringstype;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.StatistikkException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UtilstrekkeligDataException;

@AllArgsConstructor
public class Prosentkalkulator {

    public Sykefraværsdata sykefraværsdata;

    private SumAvSykefraværOverFlereKvartaler summerOppSisteFireKvartaler(
          List<UmaskertSykefraværForEttKvartal> statistikk) {

        return ekstraherSisteFireKvartaler(statistikk).stream()
              .map(SumAvSykefraværOverFlereKvartaler::new)
              .reduce(NULLPUNKT, SumAvSykefraværOverFlereKvartaler::summerOpp);
    }

    Either<StatistikkException, AggregertStatistikkDto> fraværsprosentNorge() {
        return summerOppSisteFireKvartaler(sykefraværsdata.filtrerPåKategori(LAND))
              .tilAggregertStatistikkDto(Agreggeringstype.PROSENT_SISTE_4_KVARTALER_LAND, "Norge");
    }

    Either<StatistikkException, AggregertStatistikkDto> fraværsprosentBransjeEllerNæring(
          BransjeEllerNæring bransjeEllerNæring) {
        return summerOppSisteFireKvartaler(
              sykefraværsdata.filtrerPåKategori(bransjeEllerNæring.getStatistikkategori()))
              .tilAggregertStatistikkDto(
                    StatistikkUtils.getProsenttypeFor(bransjeEllerNæring),
                    bransjeEllerNæring.getVerdiSomString());
    }

    Either<StatistikkException, AggregertStatistikkDto> fraværsprosentVirksomhet(
          String virksomhetsnavn) {
        return summerOppSisteFireKvartaler(sykefraværsdata.filtrerPåKategori(VIRKSOMHET))
              .tilAggregertStatistikkDto(
                    Agreggeringstype.PROSENT_SISTE_4_KVARTALER_VIRKSOMHET, virksomhetsnavn);
    }

    Either<UtilstrekkeligDataException, AggregertStatistikkDto> trendBransjeEllerNæring(
          BransjeEllerNæring bransjeEllerNæring) {
        Either<UtilstrekkeligDataException, Trend> maybeTrend =
              new Trendkalkulator(
                    sykefraværsdata.filtrerPåKategori(bransjeEllerNæring.getStatistikkategori()))
                    .kalkulerTrend();

        return maybeTrend.map(r -> r.tilAggregertHistorikkDto(
              StatistikkUtils.getTrendtypeFor(bransjeEllerNæring),
              bransjeEllerNæring.getVerdiSomString())
        );
    }

    private List<UmaskertSykefraværForEttKvartal> ekstraherSisteFireKvartaler(
          List<UmaskertSykefraværForEttKvartal> statistikk) {
        if (statistikk == null) {
            return List.of();
        }
        return statistikk.stream()
              .filter(datapunkt -> sisteFireKvartaler().contains(datapunkt.getÅrstallOgKvartal()))
              .collect(Collectors.toList());
    }
}