package no.nav.arbeidsgiver.sykefravarsstatistikk.api.sykefraværshistorikk;

import lombok.Data;

import java.util.List;

@Data
public class Sykefraværshistorikk {
    private SykefraværshistorikkType type;
    private String label;
    private List<KvartalsvisSykefraværsprosent> kvartalsvisSykefraværsprosent;
}
