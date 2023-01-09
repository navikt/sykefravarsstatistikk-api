package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto;

import lombok.Getter;
import lombok.Setter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class Siste4Kvartaler {
  private final BigDecimal prosent;
  private final BigDecimal tapteDagsverk;
  private final BigDecimal muligeDagsverk;
  private boolean erMaskert;
  private final List<ÅrstallOgKvartal> kvartaler;

  public Siste4Kvartaler(
      BigDecimal prosent,
      BigDecimal tapteDagsverk,
      BigDecimal muligeDagsverk,
      boolean erMaskert,
      List<ÅrstallOgKvartal> kvartaler) {
    this.prosent = prosent;
    this.tapteDagsverk = tapteDagsverk;
    this.muligeDagsverk = muligeDagsverk;
    this.erMaskert = erMaskert;
    this.kvartaler = kvartaler;
  }
}
