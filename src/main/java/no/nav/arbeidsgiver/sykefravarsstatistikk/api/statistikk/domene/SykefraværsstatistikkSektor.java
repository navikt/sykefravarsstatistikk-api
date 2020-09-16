package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.Sykefraværsstatistikk;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SykefraværsstatistikkSektor implements Sykefraværsstatistikk {
    private int årstall;
    private int kvartal;
    private String sektorkode;
    private int antallPersoner;

    private BigDecimal tapteDagsverk;
    private BigDecimal muligeDagsverk;
}
