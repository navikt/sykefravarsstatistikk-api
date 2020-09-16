package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Value;

@Value
public class Fnr {

    private final String verdi;

    public Fnr(String verdi) {
        if (!erGyldigFnr(verdi)) {
            throw new RuntimeException("Ugyldig fødselsnummer. Må bestå av 11 tegn.");
        }
        this.verdi = verdi;
    }

    public static boolean erGyldigFnr(String fnr) {
        return fnr.matches("^[0-9]{11}$");
    }

    @JsonValue
    public String asString() {
        return verdi;
    }
}
