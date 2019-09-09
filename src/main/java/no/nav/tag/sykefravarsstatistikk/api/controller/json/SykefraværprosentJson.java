package no.nav.tag.sykefravarsstatistikk.api.controller.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.nav.tag.sykefravarsstatistikk.api.sykefravarprosent.Sykefraværprosent;

import java.math.BigDecimal;

public class SykefraværprosentJson {

    @JsonProperty
    private final BigDecimal land;

    public SykefraværprosentJson(Sykefraværprosent sykefraværprosent) {
        this.land = sykefraværprosent.getLandStatistikk().beregnSykkefravarProsent();
    }
}
