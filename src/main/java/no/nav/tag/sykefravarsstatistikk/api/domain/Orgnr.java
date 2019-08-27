package no.nav.tag.sykefravarsstatistikk.api.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;

@Value
public class Orgnr implements Identifikator {
    private final String verdi;

    @Override
    @JsonValue
    public String asString() {
        return verdi;
    }
}
