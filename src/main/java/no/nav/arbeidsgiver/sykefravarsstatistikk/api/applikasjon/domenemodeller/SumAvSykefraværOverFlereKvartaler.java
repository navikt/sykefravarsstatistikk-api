package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller;

import static java.math.BigDecimal.ZERO;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.Konstanter.MIN_ANTALL_PERS_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils.StatistikkUtils.kalkulerSykefraværsprosent;

import io.vavr.control.Either;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.exceptions.StatistikkException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.exceptions.UtilstrekkeligDataException;
import org.jetbrains.annotations.NotNull;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class SumAvSykefraværOverFlereKvartaler {

  public static SumAvSykefraværOverFlereKvartaler NULLPUNKT =
      new SumAvSykefraværOverFlereKvartaler(ZERO, ZERO, 0, List.of(), List.of());

  @Getter BigDecimal muligeDagsverk;
  @Getter BigDecimal tapteDagsverk;
  private int høyesteAntallPersonerIEtKvartal;
  private List<ÅrstallOgKvartal> kvartaler;
  private List<UmaskertSykefraværForEttKvartal> umaskertSykefraværList;

  public SumAvSykefraværOverFlereKvartaler(@NotNull UmaskertSykefraværForEttKvartal umaskertSykefravær) {
    this.muligeDagsverk = umaskertSykefravær.getDagsverkNevner();
    this.tapteDagsverk = umaskertSykefravær.getDagsverkTeller();
    this.høyesteAntallPersonerIEtKvartal = umaskertSykefravær.getAntallPersoner();
    this.kvartaler = List.of(umaskertSykefravær.getÅrstallOgKvartal());
    this.umaskertSykefraværList = List.of(umaskertSykefravær);
  }

  public Either<StatistikkException, StatistikkDto> regnUtProsentOgMapTilDto(
      Statistikkategori type, String label) {

    return kalkulerFraværsprosentMedMaskering()
        .map(prosent -> this.tilStatistikkDto(type, label, prosent.toString()));
  }

  public Either<StatistikkException, SykefraværOverFlereKvartaler>
      regnUtProsentOgMapTilSykefraværForFlereKvartaler() {
    if (muligeDagsverk.compareTo(ZERO) == 0) {
      return Either.left(
          new UtilstrekkeligDataException(
              "Kan ikke regne ut sykefraværsprosent når antall mulige dagsverk er null."));
    }

    Either<StatistikkException, BigDecimal> prosent =
        kalkulerSykefraværsprosent(tapteDagsverk, muligeDagsverk);

    if (prosent.isLeft()) {
      return Either.left(prosent.getLeft());
    }

    SykefraværOverFlereKvartaler sykefraværForFlereKvartaler =
        new SykefraværOverFlereKvartaler(
            kvartaler,
            tapteDagsverk,
            muligeDagsverk,
            umaskertSykefraværList.stream()
                .map(
                    sf ->
                        new SykefraværForEttKvartal(
                            sf.getÅrstallOgKvartal(),
                            sf.getDagsverkTeller(),
                            sf.getDagsverkNevner(),
                            sf.getAntallPersoner()))
                .collect(Collectors.toList()));

    return Either.right(sykefraværForFlereKvartaler);
  }

  private Either<StatistikkException, BigDecimal> kalkulerFraværsprosentMedMaskering() {
    if (datagrunnlagetErTomt()) {
      return Either.left(new UtilstrekkeligDataException());
    }
    if (dataMåMaskeres()) {
      return Either.left(new MaskerteDataException());
    }
    if (muligeDagsverk.compareTo(ZERO) == 0) {
      return Either.left(
          new UtilstrekkeligDataException(
              "Kan ikke regne ut sykefraværsprosent når antall mulige dagsverk er null."));
    }

    return kalkulerSykefraværsprosent(tapteDagsverk, muligeDagsverk);
  }

  public Either<StatistikkException, StatistikkDto> getTapteDagsverkOgMapTilDto(
      Statistikkategori type, String virksomhetsnavn) {
    return getAntallDagsverkOgMapTilDto(type, virksomhetsnavn, this::getTapteDagsverk);
  }

  public Either<StatistikkException, StatistikkDto> getMuligeDagsverkOgMapTilDto(
      Statistikkategori type, String virksomhetsnavn) {
    return getAntallDagsverkOgMapTilDto(type, virksomhetsnavn, this::getMuligeDagsverk);
  }

  public SumAvSykefraværOverFlereKvartaler leggSammen(@NotNull SumAvSykefraværOverFlereKvartaler other) {

    return new SumAvSykefraværOverFlereKvartaler(
        this.muligeDagsverk.add(other.muligeDagsverk),
        this.tapteDagsverk.add(other.tapteDagsverk),
        Math.max(this.høyesteAntallPersonerIEtKvartal, other.høyesteAntallPersonerIEtKvartal),
        Stream.concat(kvartaler.stream(), other.kvartaler.stream())
            .distinct()
            .collect(Collectors.toList()),
        Stream.concat(umaskertSykefraværList.stream(), other.umaskertSykefraværList.stream())
            .distinct()
            .collect(Collectors.toList()));
  }

  private Either<StatistikkException, StatistikkDto> getAntallDagsverkOgMapTilDto(
      Statistikkategori type,
      String virksomhetsnavn,
      Supplier<BigDecimal> tapteEllerMuligeDagsverk) {
    if (datagrunnlagetErTomt()) {
      return Either.left(new UtilstrekkeligDataException());
    }
    if (dataMåMaskeres()) {
      return Either.left(new MaskerteDataException());
    }
    return Either.right(
        this.tilStatistikkDto(type, virksomhetsnavn, tapteEllerMuligeDagsverk.get().toString()));
  }

  private boolean datagrunnlagetErTomt() {
    return this.equals(NULLPUNKT);
  }

  private boolean dataMåMaskeres() {
    return høyesteAntallPersonerIEtKvartal
        < MIN_ANTALL_PERS_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;
  }

  private StatistikkDto tilStatistikkDto(Statistikkategori type, String label, String verdi) {
    return new StatistikkDto(type, label, verdi, høyesteAntallPersonerIEtKvartal, kvartaler);
  }

  public static class MaskerteDataException extends StatistikkException {

    public MaskerteDataException() {
      super("Ikke nok personer i datagrunnlaget - data maskeres.");
    }
  }

}
