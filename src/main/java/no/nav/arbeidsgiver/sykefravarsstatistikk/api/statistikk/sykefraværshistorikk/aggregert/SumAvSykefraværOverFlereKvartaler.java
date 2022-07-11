package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_DOWN;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Konstanter.MINIMUM_ANTALL_PERSONER_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.utils.CollectionUtils.joinLists;

import io.vavr.control.Either;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.DataException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UtilstrekkeligDataException;
import org.jetbrains.annotations.NotNull;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
class SumAvSykefraværOverFlereKvartaler {

    static SumAvSykefraværOverFlereKvartaler NULLPUNKT = new SumAvSykefraværOverFlereKvartaler(
            ZERO, ZERO, 0, List.of());

    private BigDecimal muligeDagsverk;
    private BigDecimal tapteDagsverk;
    private int antallTilfeller;
    private List<ÅrstallOgKvartal> kvartaler;


    SumAvSykefraværOverFlereKvartaler(@NotNull UmaskertSykefraværForEttKvartal data) {
        this.muligeDagsverk = data.getMuligeDagsverk();
        this.tapteDagsverk = data.getTapteDagsverk();
        this.antallTilfeller = data.getAntallPersoner();
        this.kvartaler = List.of(data.getÅrstallOgKvartal());
    }

    public Either<DataException, AggregertHistorikkDto> tilAggregertHistorikkDto(
            Statistikkategori type, String label) {

        return kalkulerFraværsprosentMedMaskering().map(prosent -> new AggregertHistorikkDto(
                type,
                label,
                prosent.toString(),
                antallTilfeller,
                kvartaler));
    }

    Either<DataException, BigDecimal> kalkulerFraværsprosentMedMaskering() {
        if (this.equals(NULLPUNKT)) {
            return Either.left(new UtilstrekkeligDataException(
                    "Trenger minst ett kvartal for å beregene sykefraværsprosent."));
        }

        if (this.antallTilfeller
                < MINIMUM_ANTALL_PERSONER_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER) {
            return Either.left(new MaskerteDataException(
                    "Ikke nok sykefraværstilfeller til å kunne vise sykefraværsprosenten."));
        }
        return Either.right(
                tapteDagsverk.divide(muligeDagsverk, 2, HALF_DOWN).multiply(new BigDecimal(100)));
    }

    SumAvSykefraværOverFlereKvartaler summerOpp(@NotNull SumAvSykefraværOverFlereKvartaler other) {
        return new SumAvSykefraværOverFlereKvartaler(
                this.muligeDagsverk.add(other.muligeDagsverk),
                this.tapteDagsverk.add(other.tapteDagsverk),
                this.antallTilfeller + other.antallTilfeller,
                joinLists(this.kvartaler, other.kvartaler));
    }
}

class MaskerteDataException extends DataException {

    public MaskerteDataException(String årsak) {
        super("Data er maskert: " + årsak);
    }
}
