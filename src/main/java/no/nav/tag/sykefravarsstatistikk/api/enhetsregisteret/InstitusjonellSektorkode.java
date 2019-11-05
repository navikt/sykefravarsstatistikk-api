package no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret;

//TODO Dette må forenes med vårt eksisterende sektor-objekt

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class InstitusjonellSektorkode {
    private final String kode;
    private final String beskrivelse;

    @JsonCreator
    public InstitusjonellSektorkode(
            @JsonProperty("kode") String kode,
            @JsonProperty("beskrivelse") String beskrivelse
    ) {
        this.kode = kode;
        this.beskrivelse = beskrivelse;
    }
}
