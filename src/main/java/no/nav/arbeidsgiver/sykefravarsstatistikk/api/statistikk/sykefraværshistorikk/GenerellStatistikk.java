package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;

import java.math.BigDecimal;
@Data
public class GenerellStatistikk {
    private final Statistikkategori type;
    private final String label;
    private final BigDecimal verdi;

}
