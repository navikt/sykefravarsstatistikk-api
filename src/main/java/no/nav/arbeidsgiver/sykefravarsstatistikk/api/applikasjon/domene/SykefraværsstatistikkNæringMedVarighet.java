package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.Sykefraværsstatistikk;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SykefraværsstatistikkNæringMedVarighet implements Sykefraværsstatistikk {
  private int årstall;
  private int kvartal;
  private String næringkode;
  private String varighet;
  private int antallPersoner;
  private BigDecimal tapteDagsverk;
  private BigDecimal muligeDagsverk;
}
