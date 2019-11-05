package no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret;

import lombok.Data;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;

// TODO Rename. Kanskje Organisasjon?
@Data
public class Enhet {
    private final Orgnr orgnr;
    private final String navn;
    private final Næringskode næringskode;
    private final InstitusjonellSektorkode institusjonellSektorkode;
}
