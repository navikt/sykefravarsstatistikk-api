package no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret;

import lombok.Builder;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
public class OverordnetEnhet implements Virksomhet {
    @NotNull
    private final Orgnr orgnr;
    @NotNull
    private final String navn;
    @NotNull
    private final Næringskode5Siffer næringskode;
    @NotNull
    private final InstitusjonellSektorkode institusjonellSektorkode;
    private final int antallAnsatte;
}
