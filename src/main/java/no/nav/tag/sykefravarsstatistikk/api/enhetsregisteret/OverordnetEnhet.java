package no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret;

import lombok.Builder;
import lombok.Data;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;

@Data
@Builder
public class OverordnetEnhet {
    private final Orgnr orgnr;
    private final String navn;
    private final Næringskode5Siffer næringskode;
    private final InstitusjonellSektorkode institusjonellSektorkode;
    private final int antallAnsatte;
}
