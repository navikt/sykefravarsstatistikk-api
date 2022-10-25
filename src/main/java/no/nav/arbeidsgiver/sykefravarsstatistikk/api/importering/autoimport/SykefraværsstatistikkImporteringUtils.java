package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhet;

public class SykefraværsstatistikkImporteringUtils {

  private final List<SykefraværsstatistikkVirksomhet> sykefraværsstatistikkVirksomhetsTestdata = Arrays.asList(
      new SykefraværsstatistikkVirksomhet(2022, 1, "910562452", "A", "2", 0,
          BigDecimal.valueOf(45.608900), BigDecimal.valueOf(0.000000)),
      new SykefraværsstatistikkVirksomhet(2022, 1, "910562452", "B", "2", 0,
          BigDecimal.valueOf(47.441176), BigDecimal.valueOf(0.000000)),
      new SykefraværsstatistikkVirksomhet(2022, 1, "910562452", "C", "2", 0,
          BigDecimal.valueOf(13.800000), BigDecimal.valueOf(0.000000)),
      new SykefraværsstatistikkVirksomhet(2022, 1, "910562452", "D", "2", 0,
          BigDecimal.valueOf(109.478935), BigDecimal.valueOf(0.000000)),
      new SykefraværsstatistikkVirksomhet(2022, 1, "910562452", "E", "2", 0,
          BigDecimal.valueOf(123.200000), BigDecimal.valueOf(0.000000)),
      new SykefraværsstatistikkVirksomhet(2022, 1, "910562452", "F", "2", 0,
          BigDecimal.valueOf(64.000000), BigDecimal.valueOf(0.000000)),
      new SykefraværsstatistikkVirksomhet(2022, 1, "910562452", "X", "2", 39,
          BigDecimal.valueOf(0.000000), BigDecimal.valueOf(2200.347300))
  );

  List<SykefraværsstatistikkVirksomhet> getSykefraværsstatistikkVirksomhetsTestdata() {
    return sykefraværsstatistikkVirksomhetsTestdata;
  }

}
