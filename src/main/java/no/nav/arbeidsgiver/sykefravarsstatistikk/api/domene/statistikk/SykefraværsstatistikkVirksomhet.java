package no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.common.Sykefraværsvarighet;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SykefraværsstatistikkVirksomhet implements Sykefraværsstatistikk {
    private int årstall;
    private int kvartal;
    private String orgnr;
    private Sykefraværsvarighet varighet;
    private int antallPersoner;
    private BigDecimal tapteDagsverk;
    private BigDecimal muligeDagsverk;
}
