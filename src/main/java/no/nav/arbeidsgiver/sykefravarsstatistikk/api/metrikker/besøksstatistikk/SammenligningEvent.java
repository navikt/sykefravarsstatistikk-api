package no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.besøksstatistikk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Fnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.enhetsregisteret.OverordnetEnhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.enhetsregisteret.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.sammenligning.Sammenligning;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.enhetsregisteret.Næringskode5Siffer;

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
