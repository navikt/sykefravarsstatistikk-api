package no.nav.tag.sykefravarsstatistikk.api.domene.bransjeprogram;

import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class Bransjeprogram {

    private final static Bransje BARNEHAGER = new Bransje("Barnehager", Arrays.asList("88911"));
    private final static Bransje NÆRINGSMIDDELINDUSTRI = new Bransje("Næringsmiddelsindustri", Arrays.asList("10"));
    private final static Bransje SYKEHUS = new Bransje("Sykehus", Arrays.asList("86101", "86102", "86104", "86105", "86106", "86107"));
    private final static Bransje SYKEHJEM = new Bransje("Næringsmiddelsindustri", Arrays.asList("87101", "87102"));
    private final static Bransje TRANSPORT = new Bransje("Transport", Arrays.asList("49100", "49311", "49391", "49392"));

    private final static List<Bransje> bransjer = Arrays.asList(BARNEHAGER, NÆRINGSMIDDELINDUSTRI, SYKEHUS, SYKEHJEM, TRANSPORT);

    public Optional<Bransje> finnBransje(Underenhet underenhet) {
        if (true) {
            // TODO Primitiv toggle for å få ut migrering uten at denne funksjonaliteten blir deployet.
            return Optional.empty();
        }
        return bransjer.stream()
                .filter(bransje -> bransje.inkludererVirksomhet(underenhet))
                .findAny();
    }
}
