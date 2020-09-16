package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles;

import lombok.Builder;
import lombok.Data;
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
