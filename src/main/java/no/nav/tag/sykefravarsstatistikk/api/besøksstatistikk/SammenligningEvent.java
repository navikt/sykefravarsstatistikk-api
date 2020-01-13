package no.nav.tag.sykefravarsstatistikk.api.besøksstatistikk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import no.nav.tag.sykefravarsstatistikk.api.domene.bransjeprogram.Bransje;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Enhet;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Næringskode5Siffer;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;

@Data
@Builder
@AllArgsConstructor
public class SammenligningEvent {
    private final Underenhet underenhet;
    private final Enhet enhet;
    private final Sektor ssbSektor;
    private final Næringskode5Siffer næring5siffer;
    private final Næring næring2siffer;
    private final Bransje bransje;
    private final Sammenligning sammenligning;
    private final String sessionId;
}
