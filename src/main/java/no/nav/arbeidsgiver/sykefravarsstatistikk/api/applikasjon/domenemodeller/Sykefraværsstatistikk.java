package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller;

import java.math.BigDecimal;

public interface Sykefraværsstatistikk {

  int getÅrstall();

  int getKvartal();

  int getAntallPersoner();

  BigDecimal getTapteDagsverk();

  BigDecimal getMuligeDagsverk();
}
