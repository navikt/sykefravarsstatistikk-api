package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.sykefraværshistorikk.KvartalsvisSykefravær;

import java.util.List;

@Data
public class LangtidOgKorttidsSykefraværshistorikk {
    @JsonProperty("korttidssykefravær")
    private SykefraværMedVarighetshistorikk korttidssykefravær;

    @JsonProperty("langtidssykefravær")
    private SykefraværMedVarighetshistorikk langtidssykefravær;

}


