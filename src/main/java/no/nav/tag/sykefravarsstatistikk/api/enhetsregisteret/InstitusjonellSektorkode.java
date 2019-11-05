package no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret;

//TODO Dette må forenes med vårt eksisterende sektor-objekt

import lombok.Value;

@Value
public class InstitusjonellSektorkode {
    private final String kode;
    private final String beskrivelse;
}
