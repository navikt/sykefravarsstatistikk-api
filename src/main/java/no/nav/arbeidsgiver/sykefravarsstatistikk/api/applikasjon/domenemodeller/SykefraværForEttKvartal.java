package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;

public class SykefraværForEttKvartal extends MaskerbartSykefravær
    implements Comparable<SykefraværForEttKvartal> {

  @JsonIgnore private final ÅrstallOgKvartal årstallOgKvartal;

  @JsonIgnore private final int antallPersoner; // TODO: trenger vi det?

  public SykefraværForEttKvartal(
      ÅrstallOgKvartal årstallOgKvartal,
      BigDecimal tapteDagsverk,
      BigDecimal muligeDagsverk,
      int antallPersoner) {
    super(
        tapteDagsverk,
        muligeDagsverk,
        antallPersoner,
        årstallOgKvartal != null && tapteDagsverk != null && muligeDagsverk != null);
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
    return Comparator.comparing(SykefraværForEttKvartal::getÅrstallOgKvartal)
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

    boolean erÅrstallOgKvartalLike = this.årstallOgKvartal.equals(that.årstallOgKvartal);
    boolean erProsentLike =
        this.getProsent() == null
            ? that.getProsent() == null
            : this.getProsent().equals(that.getProsent());
    boolean erTapteDagsverkLike =
        this.getTapteDagsverk() == null
            ? that.getTapteDagsverk() == null
            : this.getTapteDagsverk().equals(that.getTapteDagsverk());
    boolean erMuligeDagsverkLike =
        this.getMuligeDagsverk() == null
            ? that.getMuligeDagsverk() == null
            : this.getMuligeDagsverk().equals(that.getMuligeDagsverk());

    return (erÅrstallOgKvartalLike && erProsentLike && erTapteDagsverkLike && erMuligeDagsverkLike);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), årstallOgKvartal);
  }
}
