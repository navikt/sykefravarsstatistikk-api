package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_DOWN;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.utils.CollectionUtils.joinLists;

import io.vavr.control.Either;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.ManglendeDataException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import org.jetbrains.annotations.NotNull;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
class SumAvSykefravær {

    static SumAvSykefravær NULLPUNKT = new SumAvSykefravær(ZERO, ZERO, 0, List.of());

    private BigDecimal muligeDagsverk;
    private BigDecimal tapteDagsverk;
    private int antallTilfeller;
    private List<ÅrstallOgKvartal> kvartaler;

    SumAvSykefravær(@NotNull UmaskertSykefraværForEttKvartal data) {
        this.muligeDagsverk = data.getMuligeDagsverk();
        this.tapteDagsverk = data.getTapteDagsverk();
        this.antallTilfeller = data.getAntallPersoner();
        this.kvartaler = List.of(data.getÅrstallOgKvartal());
    }

    Either<ManglendeDataException, AggregertHistorikkDto> tilOppsummertStatistikkDto(
            Statistikkategori type, String label) {

        return kalkulerFraværsprosent().map(prosent -> new AggregertHistorikkDto(
                type,
                label,
                prosent.toString(),
                antallTilfeller,
                kvartaler));
    }

    Either<ManglendeDataException, BigDecimal> kalkulerFraværsprosent() {
        if (this.equals(NULLPUNKT)) {
            return Either.left(new ManglendeDataException(
                    "Trenger minst ett kvartal for å beregene sykefraværsprosent."));
        }

        return Either.right(
                tapteDagsverk.divide(muligeDagsverk, 2, HALF_DOWN).multiply(new BigDecimal(100)));
    }

    SumAvSykefravær summerOpp(@NotNull SumAvSykefravær other) {
        return new SumAvSykefravær(
                this.muligeDagsverk.add(other.muligeDagsverk),
                this.tapteDagsverk.add(other.tapteDagsverk),
                this.antallTilfeller + other.antallTilfeller,
                joinLists(this.kvartaler, other.kvartaler));
    }
}
