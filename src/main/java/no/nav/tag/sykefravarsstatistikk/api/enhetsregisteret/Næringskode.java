package no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret;

import lombok.Value;

@Value
public class Næringskode {
    private final String kode;
    private final String beskrivelse;

    public Næringskode(String kode, String beskrivelse) {
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
