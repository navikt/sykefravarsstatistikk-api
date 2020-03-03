package no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret;

import lombok.Builder;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;

@Data
@Builder
public class Enhet {
    private final Orgnr orgnr;
    private final String navn;
    private final Næringskode5Siffer næringskode;
    private final InstitusjonellSektorkode institusjonellSektorkode;
    private final int antallAnsatte;
}
