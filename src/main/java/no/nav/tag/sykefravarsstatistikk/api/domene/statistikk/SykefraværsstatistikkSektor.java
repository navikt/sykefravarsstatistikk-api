package no.nav.tag.sykefravarsstatistikk.api.domene.statistikk;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SykefraværsstatistikkSektor implements Sykefraværsstatistikk {
    private int årstall;
    private int kvartal;
    private int antallPersoner;
    private String sektorkode;

    private BigDecimal tapteDagsverk;
    private BigDecimal muligeDagsverk;
}
