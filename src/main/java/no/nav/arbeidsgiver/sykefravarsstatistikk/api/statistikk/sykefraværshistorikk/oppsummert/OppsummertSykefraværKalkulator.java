package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.oppsummert;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_DOWN;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.utils.CollectionUtils.joinLists;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.sisteFireKvartaler;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.LAND;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.VIRKSOMHET;

import io.vavr.control.Either;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.ManglendeDataException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class OppsummertSykefraværKalkulator {

    public Map<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> sykefraværsdata;

    private SummerbartSykefravær hentForSisteFireKvartaler(
            List<UmaskertSykefraværForEttKvartal> statistikk) {

        return ekstraherSisteFireKvartaler(statistikk).stream()
                .map(SummerbartSykefravær::new)
                .reduce(SummerbartSykefravær.NULLPUNKT, SummerbartSykefravær::leggSammen);
    }


    public List<UmaskertSykefraværForEttKvartal> ekstraherSisteFireKvartaler(
            List<UmaskertSykefraværForEttKvartal> statistikk) {
        if (statistikk == null) {
            return List.of();
        }
        return statistikk.stream()
                .filter(data -> sisteFireKvartaler().contains(data.getÅrstallOgKvartal()))
                .collect(Collectors.toList());
    }

    public Either<ManglendeDataException, OppsummertStatistikkDto> fraværsprosentNorge() {
        return hentForSisteFireKvartaler(sykefraværsdata.get(LAND))
                .tilGenerellStatistikkDto(LAND, "Norge");
    }

    public Either<ManglendeDataException, OppsummertStatistikkDto> fraværsprosentBransjeEllerNæring(
            BransjeEllerNæring bransjeEllerNæring) {
        return hentForSisteFireKvartaler(
                sykefraværsdata.get(bransjeEllerNæring.getStatistikkategori()))
                .tilGenerellStatistikkDto(
                        bransjeEllerNæring.getStatistikkategori(),
                        bransjeEllerNæring.getVerdiSomString());
    }

    public Either<ManglendeDataException, OppsummertStatistikkDto> sykefraværVirksomhet(
            String virksomhetsnavn) {
        return hentForSisteFireKvartaler(sykefraværsdata.get(VIRKSOMHET))
                .tilGenerellStatistikkDto(VIRKSOMHET, virksomhetsnavn);
    }

    public Either<ManglendeDataException, OppsummertStatistikkDto> trendBransjeEllerNæring(
            BransjeEllerNæring bransjeEllerNæring) {
        Either<ManglendeDataException, TrendKalkulator> maybeTrend = TrendKalkulator.kalkulerTrend(
                sykefraværsdata.get(bransjeEllerNæring.getTrendkategori()));

        return maybeTrend.map(r -> r.tilOppsummertStatistikkDto(
                bransjeEllerNæring.getTrendkategori(),
                bransjeEllerNæring.getVerdiSomString())
        );
    }
}

@ToString
@EqualsAndHashCode
@AllArgsConstructor
class SummerbartSykefravær {

    static SummerbartSykefravær NULLPUNKT = new SummerbartSykefravær(ZERO, ZERO, 0, List.of());

    public BigDecimal muligeDagsverk;
    public BigDecimal tapteDagsverk;
    public int antallTilfeller;
    public List<ÅrstallOgKvartal> kvartaler;

    SummerbartSykefravær(@NotNull UmaskertSykefraværForEttKvartal data) {
        this.muligeDagsverk = data.getMuligeDagsverk();
        this.tapteDagsverk = data.getTapteDagsverk();
        this.antallTilfeller = data.getAntallPersoner();
        this.kvartaler = List.of(data.getÅrstallOgKvartal());
    }

    public Either<ManglendeDataException, OppsummertStatistikkDto> tilGenerellStatistikkDto(
            Statistikkategori type, String label) {

        return kalkulerFraværsprosent().map(prosent -> new OppsummertStatistikkDto(
                type,
                label,
                prosent.toString(),
                antallTilfeller,
                kvartaler));
    }

    Either<ManglendeDataException, BigDecimal> kalkulerFraværsprosent() {
        if (this.equals(NULLPUNKT)) {
            return Either.left(new ManglendeDataException("Ingen sykefraværsdata tilgjengelig."));
        }

        return Either.right(
                tapteDagsverk.divide(muligeDagsverk, 2, HALF_DOWN).multiply(new BigDecimal(100)));
    }

    SummerbartSykefravær leggSammen(@NotNull SummerbartSykefravær other) {
        return new SummerbartSykefravær(
                this.muligeDagsverk.add(other.muligeDagsverk),
                this.tapteDagsverk.add(other.tapteDagsverk),
                this.antallTilfeller + other.antallTilfeller,
                joinLists(this.kvartaler, other.kvartaler));
    }
}