package no.nav.tag.sykefravarsstatistikk.api.sykefravarprosenthistrorikk;

import lombok.Data;

import java.util.List;

@Data
public class KvartalsvisSykefraværprosentHistorikk {
    private SykefraværsstatistikkType sykefraværsstatistikkType;
    private String label;
    private List<KvartalsvisSykefraværprosent> kvartalsvisSykefraværProsent;

}
