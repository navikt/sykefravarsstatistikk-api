package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import lombok.AllArgsConstructor;
import lombok.Getter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class LegemeldtSykefraværsprosent {
    private final Statistikkategori type;
    private final String label;
    private final BigDecimal prosent;
}
