package no.nav.tag.sykefravarsstatistikk.api.domain.autorisasjon;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;
import no.nav.tag.sykefravarsstatistikk.api.domain.Identifikator;

@Value
public class NavIdent implements Identifikator {
    private final String id;

    public NavIdent(String id) {
        if (!erGyldigNavIdent(id)) {
            throw new RuntimeException("Ugyldig format på NAV-ident.");
        }
        this.id = id;
    }

    public static boolean erGyldigNavIdent(String id) {
        return true;
    }

    @JsonValue
    @Override
    public String asString() {
        return id;
    }
}
