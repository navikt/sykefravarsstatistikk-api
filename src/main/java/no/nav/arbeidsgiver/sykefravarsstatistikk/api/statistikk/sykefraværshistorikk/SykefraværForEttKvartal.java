package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;

public class SykefraværForEttKvartal extends MaskerbartSykefravær implements
    Comparable<SykefraværForEttKvartal> {

  @JsonIgnore
  private final ÅrstallOgKvartal årstallOgKvartal;

  @JsonIgnore
  private final int antallPersoner; // TODO: trenger vi det?

  public SykefraværForEttKvartal(
      ÅrstallOgKvartal årstallOgKvartal,
      BigDecimal tapteDagsverk,
      BigDecimal muligeDagsverk,
      int antallPersoner
  ) {
    super(
        tapteDagsverk,
        muligeDagsverk,
        antallPersoner,
        årstallOgKvartal != null
    );
    this.årstallOgKvartal = årstallOgKvartal;
    this.antallPersoner = antallPersoner;
  }

  public int getKvartal() {
    return årstallOgKvartal != null ? årstallOgKvartal.getKvartal() : 0;
  }

  public int getÅrstall() {
    return årstallOgKvartal != null ? årstallOgKvartal.getÅrstall() : 0;
  }

  public int getAntallPersoner() {
    return antallPersoner;
  }

  @Override
  public int compareTo(SykefraværForEttKvartal sykefraværForEttKvartal) {
    return Comparator
        .comparing(SykefraværForEttKvartal::getÅrstallOgKvartal)
        .compare(this, sykefraværForEttKvartal);
  }

  public ÅrstallOgKvartal getÅrstallOgKvartal() {
    return årstallOgKvartal;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SykefraværForEttKvartal)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    SykefraværForEttKvartal that = (SykefraværForEttKvartal) o;

    return (årstallOgKvartal.equals(that.årstallOgKvartal)
        && getProsent() == null ?
        that.getProsent() == null : getProsent().equals(that.getProsent())
        && getTapteDagsverk() == null ?
        that.getTapteDagsverk() == null : getTapteDagsverk().equals(that.getTapteDagsverk())
        && getMuligeDagsverk() == null ?
        that.getMuligeDagsverk() == null : getMuligeDagsverk().equals(that.getMuligeDagsverk())
    );
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), årstallOgKvartal);
  }
}
