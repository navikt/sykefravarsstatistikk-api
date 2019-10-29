package no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Næringsgruppe implements Virksomhetsklassifikasjon {
    private String kode;
    private String navn;

}
