package no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Næring implements Virksomhetsklassifikasjon {
    private String næringsgruppeKode;
    private String kode;
    private String navn;

}
