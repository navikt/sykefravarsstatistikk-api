package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static java.math.BigDecimal.ZERO;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Konstanter.MIN_ANTALL_PERS_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.utils.CollectionUtils.concat;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.StatistikkUtils.kalkulerSykefraværsprosent;

import io.vavr.control.Either;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.MaskerteDataException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.StatistikkException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UtilstrekkeligDataException;
import org.jetbrains.annotations.NotNull;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
class SumAvSykefraværOverFlereKvartaler {

    static SumAvSykefraværOverFlereKvartaler NULLPUNKT =
          new SumAvSykefraværOverFlereKvartaler(ZERO, ZERO, 0, List.of());

    @Getter
    BigDecimal muligeDagsverk;
    @Getter
    BigDecimal tapteDagsverk;
    private int antallPersoner;
    private List<ÅrstallOgKvartal> kvartaler;


    SumAvSykefraværOverFlereKvartaler(@NotNull UmaskertSykefraværForEttKvartal data) {
        this.muligeDagsverk = data.getMuligeDagsverk();
        this.tapteDagsverk = data.getTapteDagsverk();
        this.antallPersoner = data.getAntallPersoner();
        this.kvartaler = List.of(data.getÅrstallOgKvartal());
    }


    Either<StatistikkException, StatistikkDto> regnUtProsentOgMapTilDto
          (Statistikkategori type, String label) {

        return kalkulerFraværsprosentMedMaskering().map(
              prosent -> this.tilStatistikkDto(type, label, prosent.toString())
        );
    }


    private Either<StatistikkException, BigDecimal> kalkulerFraværsprosentMedMaskering() {
        if (datagrunnlagetErTomt()) {
            return Either.left(new UtilstrekkeligDataException());
        }
        if (dataMåMaskeres()) {
            return Either.left(new MaskerteDataException());
        }
        if (muligeDagsverk.equals(ZERO)) {
            return Either.left(new UtilstrekkeligDataException(
                  "Kan ikke regne ut sykefraværsprosent når antall mulige dagsverk er null."));
        }

        return Either.right(kalkulerSykefraværsprosent(tapteDagsverk, muligeDagsverk));
    }


    Either<StatistikkException, StatistikkDto> getTapteDagsverkOgMapTilDto(
          Statistikkategori type, String virksomhetsnavn
    ) {
        return getAntallDagsverkOgMapTilDto(type, virksomhetsnavn, this::getTapteDagsverk);
    }


    Either<StatistikkException, StatistikkDto> getMuligeDagsverkOgMapTilDto(
          Statistikkategori type, String virksomhetsnavn
    ) {
        return getAntallDagsverkOgMapTilDto(type, virksomhetsnavn, this::getMuligeDagsverk);
    }


    SumAvSykefraværOverFlereKvartaler leggSammen(@NotNull SumAvSykefraværOverFlereKvartaler other) {
        return new SumAvSykefraværOverFlereKvartaler(
              this.muligeDagsverk.add(other.muligeDagsverk),
              this.tapteDagsverk.add(other.tapteDagsverk),
              this.antallPersoner + other.antallPersoner,
              concat(this.kvartaler, other.kvartaler));
    }


    private Either<StatistikkException, StatistikkDto> getAntallDagsverkOgMapTilDto(
          Statistikkategori type, String virksomhetsnavn,
          Supplier<BigDecimal> tapteEllerMuligeDagsverk
    ) {
        if (datagrunnlagetErTomt()) {
            return Either.left(new UtilstrekkeligDataException());
        }
        if (dataMåMaskeres()) {
            return Either.left(new MaskerteDataException());
        }
        return Either.right(
              this.tilStatistikkDto(
                    type, virksomhetsnavn, tapteEllerMuligeDagsverk.get().toString())
        );
    }


    private boolean datagrunnlagetErTomt() {
        return this.equals(NULLPUNKT);
    }


    private boolean dataMåMaskeres() {
        return antallPersoner
              < MIN_ANTALL_PERS_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;
    }


    private StatistikkDto tilStatistikkDto(Statistikkategori type, String label, String verdi) {
        return StatistikkDto.builder()
              .statistikkategori(type)
              .label(label)
              .verdi(verdi)
              .antallPersonerIBeregningen(antallPersoner)
              .kvartalerIBeregningen(kvartaler)
              .build();
    }

}
