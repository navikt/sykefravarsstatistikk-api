package no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.besøksstatistikk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.besøksstatistikk.sammenligning.Sammenligning;

@Data
@Builder
@AllArgsConstructor
public class SammenligningEvent {
    private final Underenhet underenhet;
    private final OverordnetEnhet overordnetEnhet;
    private final Sektor ssbSektor;
    private final Næringskode5Siffer næring5siffer;
    private final Næring næring2siffer;
    private final Bransje bransje;
    private final Fnr fnr;
    private final Sammenligning sammenligning;
    private final String sessionId;
}
