package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.Sykefraværsstatistikk;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SykefraværsstatistikkNæring5Siffer implements Sykefraværsstatistikk {
  private int årstall;
  private int kvartal;
  private String næringkode5siffer;
  private int antallPersoner;

  private BigDecimal tapteDagsverk;
  private BigDecimal muligeDagsverk;

  // Kotlin kjenner ikke til @Data annotation (Lombok)
  public String getNæringkode5siffer() {
    return næringkode5siffer;
  }
}
