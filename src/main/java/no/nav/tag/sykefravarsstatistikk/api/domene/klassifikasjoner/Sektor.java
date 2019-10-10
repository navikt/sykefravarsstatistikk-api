package no.nav.tag.sykefravarsstatistikk.api.domene.klassifikasjoner;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Sektor {
    private String kode;
    private String navn;
}
