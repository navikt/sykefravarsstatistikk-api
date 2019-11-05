package no.nav.tag.sykefravarsstatistikk.api.domene.statistikk;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Virksomhetsklassifikasjon;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SykefraværsstatistikkLand implements Sykefraværsstatistikk {
    private int år;
    private int kvartal;

    private BigDecimal tapteDagsverk;
    private BigDecimal muligeDagsverk;
}
