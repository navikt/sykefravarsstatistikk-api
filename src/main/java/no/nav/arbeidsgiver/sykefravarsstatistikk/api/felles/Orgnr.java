package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;

@Value
public class Orgnr {
    private final String verdi;

    @JsonValue
    public String asString() {
        return verdi;
    }
}
