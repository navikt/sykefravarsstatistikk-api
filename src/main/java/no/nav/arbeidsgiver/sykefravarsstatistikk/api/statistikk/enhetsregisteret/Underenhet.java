package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.enhetsregisteret;

import lombok.Builder;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;

@Builder
@Data
public class Underenhet implements Virksomhet {
    private final Orgnr orgnr;
    private final Orgnr overordnetEnhetOrgnr;
    private final String navn;
    private final Næringskode5Siffer næringskode;
    private final int antallAnsatte;
}
