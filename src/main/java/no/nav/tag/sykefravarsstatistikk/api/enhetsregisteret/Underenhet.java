package no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret;

import lombok.Builder;
import lombok.Data;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;

@Builder
@Data
public class Underenhet {
    private final Orgnr orgnr;
    private final Orgnr overordnetEnhetOrgnr;
    private final String navn;
    private final Næringskode5Siffer næringskode;
    private final int antallAnsatte;
}
