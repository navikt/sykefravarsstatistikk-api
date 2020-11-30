package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SykefraværsstatistikkVirksomhetGradering implements Sykefraværsstatistikk {
    private int årstall;
    private int kvartal;
    private String orgnr;
    private String næringkode;
    private int antallPersoner;
    private BigDecimal tapteDagsverk;
    private BigDecimal muligeDagsverk;
}
