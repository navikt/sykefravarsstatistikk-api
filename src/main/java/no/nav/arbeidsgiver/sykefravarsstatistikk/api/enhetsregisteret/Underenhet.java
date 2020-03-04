package no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret;

import lombok.Builder;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;

@Builder
@Data
public class Underenhet implements Virksomhet {
    private final Orgnr orgnr;
    private final Orgnr overordnetEnhetOrgnr;
    private final String navn;
    private final Næringskode5Siffer næringskode;
    private final int antallAnsatte;
}
