package no.nav.tag.sykefravarsstatistikk.api.tapteDagsverkForKostnadsberegning;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class TapteDagsverk {
    private BigDecimal tapteDagsverk;
    private int årstall;
    private int kvartal;

    @JsonIgnore
    public int getArstall() {
        return årstall;
    }
}
