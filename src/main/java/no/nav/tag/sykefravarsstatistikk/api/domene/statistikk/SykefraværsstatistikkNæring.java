package no.nav.tag.sykefravarsstatistikk.api.domene.statistikk;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SykefraværsstatistikkNæring implements Sykefraværsstatistikk {
    private int årstall;
    private int kvartal;
    private String næringkode;
    private int antallPersoner;

    private BigDecimal tapteDagsverk;
    private BigDecimal muligeDagsverk;
}
