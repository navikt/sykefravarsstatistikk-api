package no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class Næringskode {
    private final String kode;
    private final String beskrivelse;

    @JsonCreator
    public Næringskode(
            @JsonProperty("kode") String kode,
            @JsonProperty("beskrivelse") String beskrivelse
    ) {
        this.beskrivelse = beskrivelse;

        String næringskodeUtenPunktum = kode.replace(".", "");

        if (erGyldigNæringskode(næringskodeUtenPunktum)) {
            this.kode = næringskodeUtenPunktum;
        } else {
            throw new RuntimeException("Ugyldig næringskode. Må bestå av 5 siffer.");
        }
    }

    private boolean erGyldigNæringskode(String verdi) {
        return verdi.matches("^[0-9]{5}$");
    }
}
