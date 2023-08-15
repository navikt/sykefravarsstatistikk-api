package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class SykefraværsstatistikkLand implements Sykefraværsstatistikk {
  private int årstall;
  private int kvartal;
  private int antallPersoner;

  private BigDecimal tapteDagsverk;
  private BigDecimal muligeDagsverk;
}
