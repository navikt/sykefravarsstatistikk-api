package no.nav.tag.sykefravarsstatistikk.api.domene.bransjeprogram;

import lombok.Value;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;

import java.util.List;

@Value
public class Bransje {
    private final String navn;
    private final List<String> koderSomSpesifisererNæringer;

    public boolean inkludererVirksomhet(Underenhet underenhet) {
        String næringskode = underenhet.getNæringskode().getKode();
        return koderSomSpesifisererNæringer.stream().anyMatch(næringskode::startsWith);
    }
}
