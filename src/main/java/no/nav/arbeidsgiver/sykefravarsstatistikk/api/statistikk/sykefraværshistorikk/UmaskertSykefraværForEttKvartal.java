package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import io.vavr.control.Either;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.StatistikkUtils;
import org.jetbrains.annotations.NotNull;

@Data
public class UmaskertSykefraværForEttKvartal implements
    Comparable<UmaskertSykefraværForEttKvartal> {

  protected final BigDecimal dagsverkTeller;
  protected final BigDecimal dagsverkNevner;
  protected final int antallPersoner;
  private final ÅrstallOgKvartal årstallOgKvartal;

  public UmaskertSykefraværForEttKvartal(
      ÅrstallOgKvartal årstallOgKvartal,
      BigDecimal dagsverkTeller,
      BigDecimal dagsverkNevner,
      int antallPersoner) {
    this.årstallOgKvartal = årstallOgKvartal;
    this.dagsverkTeller = dagsverkTeller.setScale(1, RoundingMode.HALF_UP);
    this.dagsverkNevner = dagsverkNevner.setScale(1, RoundingMode.HALF_UP);
    this.antallPersoner = antallPersoner;
  }

  public UmaskertSykefraværForEttKvartal(
      ÅrstallOgKvartal kvartal,
      int dagsverkTeller,
      int dagsverkNevner,
      int antallPersoner) {
    this.årstallOgKvartal = kvartal;
    this.dagsverkTeller = new BigDecimal(String.valueOf(dagsverkTeller));
    this.dagsverkNevner = new BigDecimal(String.valueOf(dagsverkNevner));
    this.antallPersoner = antallPersoner;
  }

  public static Optional<UmaskertSykefraværForEttKvartal> hentUtKvartal(
      Collection<UmaskertSykefraværForEttKvartal> sykefravær,
      @NotNull ÅrstallOgKvartal kvartal) {
    return (sykefravær == null)
        ? Optional.empty()
        : sykefravær.stream()
            .filter(datapunkt -> datapunkt.getÅrstallOgKvartal().equals(kvartal))
            .findAny();
  }

  public int getKvartal() {
    return årstallOgKvartal != null ? årstallOgKvartal.getKvartal() : 0;
  }

  public int getÅrstall() {
    return årstallOgKvartal != null ? årstallOgKvartal.getÅrstall() : 0;
  }

  public Either<StatistikkException, BigDecimal> kalkulerSykefraværsprosent() {
    return StatistikkUtils.kalkulerSykefraværsprosent(dagsverkTeller, dagsverkNevner);
  }


  public UmaskertSykefraværForEttKvartal add(UmaskertSykefraværForEttKvartal other) {
    if (!other.getÅrstallOgKvartal().equals(årstallOgKvartal)) {
      throw new IllegalArgumentException(
          "Kan ikke summere kvartalsvis sykefravær med forskjellige kvartaler");
    }
    return new UmaskertSykefraværForEttKvartal(
        årstallOgKvartal,
        dagsverkTeller.add(other.getDagsverkTeller()),
        dagsverkNevner.add(other.getDagsverkNevner()),
        antallPersoner + other.getAntallPersoner()
    );
  }

  @Override
  public int compareTo(@NotNull UmaskertSykefraværForEttKvartal kvartalsvisSykefravær) {
    return Comparator
        .comparing(UmaskertSykefraværForEttKvartal::getÅrstallOgKvartal)
        .compare(this, kvartalsvisSykefravær);
  }
}
