package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SykefraværsstatistikkSektor implements Sykefraværsstatistikk {
  private int årstall;
  private int kvartal;
  private String sektorkode;
  private int antallPersoner;

  private BigDecimal tapteDagsverk;
  private BigDecimal muligeDagsverk;

  // Kotlin kjenner ikke til @Data annotation (Lombok)
  public String getSektorkode() {
    return sektorkode;
  }
}
