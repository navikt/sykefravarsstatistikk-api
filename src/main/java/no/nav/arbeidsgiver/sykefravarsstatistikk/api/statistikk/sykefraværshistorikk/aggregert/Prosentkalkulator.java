package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.sisteFireKvartaler;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.LAND;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.VIRKSOMHET;

import io.vavr.control.Either;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.ManglendeDataException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;

@AllArgsConstructor
public class Prosentkalkulator {

    public Historikkdata sykefraværsdata;

    private SumAvSykefravær summerOppSisteFireKvartaler(
            List<UmaskertSykefraværForEttKvartal> statistikk) {

        return ekstraherSisteFireKvartaler(statistikk).stream()
                .map(SumAvSykefravær::new)
                .reduce(SumAvSykefravær.NULLPUNKT, SumAvSykefravær::summerOpp);
    }

    Either<ManglendeDataException, AggregertHistorikkDto> fraværsprosentNorge() {
        return summerOppSisteFireKvartaler(sykefraværsdata.hentFor(LAND))
                .tilOppsummertStatistikkDto(LAND, "Norge");
    }

    Either<ManglendeDataException, AggregertHistorikkDto> fraværsprosentBransjeEllerNæring(
            BransjeEllerNæring bransjeEllerNæring) {
        return summerOppSisteFireKvartaler(
                sykefraværsdata.hentFor(bransjeEllerNæring.getStatistikkategori()))
                .tilOppsummertStatistikkDto(
                        bransjeEllerNæring.getStatistikkategori(),
                        bransjeEllerNæring.getVerdiSomString());
    }

    Either<ManglendeDataException, AggregertHistorikkDto> fraværsprosentVirksomhet(
            String virksomhetsnavn) {
        return summerOppSisteFireKvartaler(sykefraværsdata.hentFor(VIRKSOMHET))
                .tilOppsummertStatistikkDto(VIRKSOMHET, virksomhetsnavn);
    }

    Either<ManglendeDataException, AggregertHistorikkDto> trendBransjeEllerNæring(
            BransjeEllerNæring bransjeEllerNæring) {
        Either<ManglendeDataException, Trendkalkulator> maybeTrend = Trendkalkulator.kalkulerTrend(
                sykefraværsdata.hentFor(bransjeEllerNæring.getTrendkategori()));

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