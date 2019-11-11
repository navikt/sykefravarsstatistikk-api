package no.nav.tag.sykefravarsstatistikk.api.domene.statistikk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Virksomhetsklassifikasjon;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class SykefraværsstatistikkLand implements Sykefraværsstatistikk {
    private int årstall;
    private int kvartal;

    private BigDecimal tapteDagsverk;
    private BigDecimal muligeDagsverk;
}
