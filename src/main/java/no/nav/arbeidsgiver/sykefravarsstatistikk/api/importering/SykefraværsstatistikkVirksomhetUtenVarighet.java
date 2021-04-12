package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
// TODO: rename SykefraværsstatistikkVirksomhet til SykefraværsstatistikkVirksomhetMedVarighet og denne til SykefraværsstatistikkVirksomhet
public class SykefraværsstatistikkVirksomhetUtenVarighet implements Sykefraværsstatistikk {
    private int årstall;
    private int kvartal;
    private String orgnr;
    private int antallPersoner;
    private BigDecimal tapteDagsverk;
    private BigDecimal muligeDagsverk;
}
