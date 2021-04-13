package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering;

import java.math.BigDecimal;

public interface Sykefraværsstatistikk {

    int getÅrstall();
    int getKvartal();
    BigDecimal getTapteDagsverk();
    BigDecimal getMuligeDagsverk();
}
