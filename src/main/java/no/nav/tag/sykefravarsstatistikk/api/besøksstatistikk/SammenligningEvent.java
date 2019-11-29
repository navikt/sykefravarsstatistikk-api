package no.nav.tag.sykefravarsstatistikk.api.besøksstatistikk;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Enhet;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;

@Data
@AllArgsConstructor
public class SammenligningEvent {
    private final Underenhet underenhet;
    private final Enhet enhet;
    private final Sektor ssbSektor;
    private final Næring næring;
    private final Sammenligning sammenligning;
}
