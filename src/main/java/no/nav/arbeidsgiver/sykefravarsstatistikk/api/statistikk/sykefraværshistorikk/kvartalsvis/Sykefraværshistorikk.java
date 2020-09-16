package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.sykefravær.KvartalsvisSykefravær;

import java.util.List;

@Data
public class Sykefraværshistorikk {
    private SykefraværshistorikkType type;
    private String label;

    @JsonProperty("kvartalsvisSykefraværsprosent")
    private List<KvartalsvisSykefravær> kvartalsvisSykefravær;
}
