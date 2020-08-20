package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SykefraværMedVarighetshistorikk {
    private String varighet; // langtid | korttid

    @JsonProperty("siste4KvartalerSykefravær")
    private Siste4KvartalerSykefraværsprosent siste4KvartalerSykefraværsprosent;
}


