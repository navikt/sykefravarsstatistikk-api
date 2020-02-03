package no.nav.tag.sykefravarsstatistikk.api.sykefravarprosenthistrorikk;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

@Data
@JsonPropertyOrder({ "label", "kvartalsvisSykefraværProsent" })
public class KvartalsvisSykefraværprosentHistorikk {
    private String label;
    private List<KvartalsvisSykefraværprosent> kvartalsvisSykefraværProsent;

}
