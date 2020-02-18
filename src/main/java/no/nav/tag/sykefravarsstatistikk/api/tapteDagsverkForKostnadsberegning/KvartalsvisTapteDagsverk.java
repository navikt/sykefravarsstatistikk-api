package no.nav.tag.sykefravarsstatistikk.api.tapteDagsverkForKostnadsberegning;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;
import no.nav.tag.sykefravarsstatistikk.api.common.Konstanter;

import java.math.BigDecimal;

@Value
public class KvartalsvisTapteDagsverk {
    private BigDecimal tapteDagsverk;
    private int årstall;
    private int kvartal;
    private boolean erMaskert;

    @JsonIgnore
    private Integer antallPersoner;


    public KvartalsvisTapteDagsverk(BigDecimal tapteDagsverk, int årstall, int kvartal, int antallPersoner) {
        this.årstall = årstall;
        this.kvartal = kvartal;
        if (antallPersoner >= Konstanter.MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER) {
            this.erMaskert = false;
            this.tapteDagsverk = tapteDagsverk;
            this.antallPersoner = antallPersoner;
        } else {
            this.erMaskert = true;
            this.tapteDagsverk = null;
            this.antallPersoner = null;
        }
    }
}
