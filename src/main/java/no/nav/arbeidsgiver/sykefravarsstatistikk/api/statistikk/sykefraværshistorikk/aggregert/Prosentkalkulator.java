package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.sisteFireKvartaler;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.LAND;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.VIRKSOMHET;

import io.vavr.control.Either;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.DataException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UtilstrekkeligDataException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;

@AllArgsConstructor
public class Prosentkalkulator {

    public Sykefraværsdata sykefraværsdata;

    private SumAvSykefraværOverFlereKvartaler summerOppSisteFireKvartaler(
            List<UmaskertSykefraværForEttKvartal> statistikk) {

        return ekstraherSisteFireKvartaler(statistikk).stream()
                .map(SumAvSykefraværOverFlereKvartaler::new)
                .reduce(SumAvSykefraværOverFlereKvartaler.NULLPUNKT, SumAvSykefraværOverFlereKvartaler::summerOpp);
    }

    Either<DataException, AggregertHistorikkDto> fraværsprosentNorge() {
        return summerOppSisteFireKvartaler(sykefraværsdata.hentUtFor(LAND))
                .tilAggregertHistorikkDto(LAND, "Norge");
    }

    Either<DataException, AggregertHistorikkDto> fraværsprosentBransjeEllerNæring(
            BransjeEllerNæring bransjeEllerNæring) {
        return summerOppSisteFireKvartaler(
                sykefraværsdata.hentUtFor(bransjeEllerNæring.getStatistikkategori()))
                .tilAggregertHistorikkDto(
                        bransjeEllerNæring.getStatistikkategori(),
                        bransjeEllerNæring.getVerdiSomString());
    }

    Either<DataException, AggregertHistorikkDto> fraværsprosentVirksomhet(
            String virksomhetsnavn) {
        return summerOppSisteFireKvartaler(sykefraværsdata.hentUtFor(VIRKSOMHET))
                .tilAggregertHistorikkDto(VIRKSOMHET, virksomhetsnavn);
    }

    Either<UtilstrekkeligDataException, AggregertHistorikkDto> trendBransjeEllerNæring(
            BransjeEllerNæring bransjeEllerNæring) {
        Either<UtilstrekkeligDataException, Trend> maybeTrend =
                new Trendkalkulator(sykefraværsdata.hentUtFor(bransjeEllerNæring.getStatistikkategori()))
                        .kalkulerTrend();

        return maybeTrend.map(r -> r.tilAggregertHistorikkDto(
                bransjeEllerNæring.getTrendkategori(),
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