package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class SykefraværOverFlereKvartaler extends MaskerbartSykefraværOverFlereKvartaler {

  private List<ÅrstallOgKvartal> kvartaler;

  public SykefraværOverFlereKvartaler(
      List<ÅrstallOgKvartal> kvartaler,
      BigDecimal tapteDagsverk,
      BigDecimal muligeDagsverk,
      List<SykefraværForEttKvartal> sykefraværList) {
    super(tapteDagsverk, muligeDagsverk, sykefraværList, kvartaler.size() != 0);
    this.kvartaler = kvartaler;
  }

  public List<ÅrstallOgKvartal> getKvartaler() {
    return kvartaler;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SykefraværOverFlereKvartaler)) return false;
    if (!super.equals(o)) return false;
    SykefraværOverFlereKvartaler that = (SykefraværOverFlereKvartaler) o;
    return (kvartaler.equals(that.kvartaler)
        && getProsent().equals(that.getProsent())
        && getTapteDagsverk().equals(that.getTapteDagsverk())
        && getMuligeDagsverk().equals(that.getMuligeDagsverk()));
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), kvartaler);
  }
}
