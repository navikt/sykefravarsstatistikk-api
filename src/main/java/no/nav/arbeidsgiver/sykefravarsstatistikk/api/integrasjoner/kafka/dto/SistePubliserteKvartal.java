package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SistePubliserteKvartal {
  private int årstall;
  private int kvartal;
  private final BigDecimal prosent;
  private final BigDecimal tapteDagsverk;
  private final BigDecimal muligeDagsverk;
  private int antallPersoner;
  private boolean erMaskert;

  public SistePubliserteKvartal(
      int årstall,
      int kvartal,
      BigDecimal prosent,
      BigDecimal tapteDagsverk,
      BigDecimal muligeDagsverk,
      int antallPersoner,
      boolean erMaskert) {
    this.årstall = årstall;
    this.kvartal = kvartal;
    this.prosent = prosent;
    this.tapteDagsverk = tapteDagsverk;
    this.muligeDagsverk = muligeDagsverk;
    this.antallPersoner = antallPersoner;
    this.erMaskert = erMaskert;
  }
}
