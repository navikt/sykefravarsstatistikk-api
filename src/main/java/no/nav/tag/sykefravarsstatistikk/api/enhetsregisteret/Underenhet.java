package no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret;

import lombok.Data;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;

// TODO Rename. Kanskje Organisasjon?
@Data
public class Underenhet {
    private final Orgnr orgnr;
    private final Orgnr overordnetEnhetOrgnr;
    private final String navn;
    private final Næringskode næringskode;
    private final InstitusjonellSektorkode institusjonellSektorkode;
}
