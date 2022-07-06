package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.oppsummert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.sisteFireKvartaler;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.LAND;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.VIRKSOMHET;

import io.vavr.control.Either;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.ManglendeDataException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;

@AllArgsConstructor
public class Prosentkalkulator {

    public Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> sykefraværsdata;

    private SumAvSykefravær summerOppSisteFireKvartaler(
            List<UmaskertSykefraværForEttKvartal> statistikk) {

        return ekstraherSisteFireKvartaler(statistikk).stream()
                .map(SumAvSykefravær::new)
                .reduce(SumAvSykefravær.NULLPUNKT, SumAvSykefravær::summerOpp);
    }

    Either<ManglendeDataException, OppsummertStatistikkDto> fraværsprosentNorge() {
        return summerOppSisteFireKvartaler(sykefraværsdata.get(LAND))
                .tilOppsummertStatistikkDto(LAND, "Norge");
    }

    Either<ManglendeDataException, OppsummertStatistikkDto> fraværsprosentBransjeEllerNæring(
            BransjeEllerNæring bransjeEllerNæring) {
        return summerOppSisteFireKvartaler(
                sykefraværsdata.get(bransjeEllerNæring.getStatistikkategori()))
                .tilOppsummertStatistikkDto(
                        bransjeEllerNæring.getStatistikkategori(),
                        bransjeEllerNæring.getVerdiSomString());
    }

    Either<ManglendeDataException, OppsummertStatistikkDto> sykefraværVirksomhet(
            String virksomhetsnavn) {
        return summerOppSisteFireKvartaler(sykefraværsdata.get(VIRKSOMHET))
                .tilOppsummertStatistikkDto(VIRKSOMHET, virksomhetsnavn);
    }

    Either<ManglendeDataException, OppsummertStatistikkDto> trendBransjeEllerNæring(
            BransjeEllerNæring bransjeEllerNæring) {
        Either<ManglendeDataException, Trendkalkulator> maybeTrend = Trendkalkulator.kalkulerTrend(
                sykefraværsdata.get(bransjeEllerNæring.getTrendkategori()));

        return maybeTrend.map(r -> r.tilOppsummertStatistikkDto(
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

