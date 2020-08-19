package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.common.Sykefraværsvarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.sykefraværshistorikk.KvartalsvisSykefravær;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.sykefraværshistorikk.SykefraværshistorikkType;

import java.util.List;

@Data
public class SykefraværMedVarighetshistorikk {
    private String varighet; // langtid | korttid

    @JsonProperty("kvartalsvisSykefraværsprosent")
    private List<KvartalsvisSykefravær> kvartalsvisSykefravær;
}


