package no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Sektor {
    private String kode;
    private String navn;
}
