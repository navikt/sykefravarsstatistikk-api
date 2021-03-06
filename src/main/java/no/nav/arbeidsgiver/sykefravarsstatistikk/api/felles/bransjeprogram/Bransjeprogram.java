package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class Bransjeprogram {

    private final static Bransje BARNEHAGER = new Bransje(
            Bransjetype.BARNEHAGER,
            "Barnehager",
            "88911"
    );
    private final static Bransje NÆRINGSMIDDELINDUSTRI = new Bransje(
            Bransjetype.NÆRINGSMIDDELINDUSTRI,
            "Næringsmiddelsindustrien",
            "10"
    );
    private final static Bransje SYKEHUS = new Bransje(
            Bransjetype.SYKEHUS,
            "Sykehus",
            "86101", "86102", "86104", "86105", "86106", "86107"
    );
    private final static Bransje SYKEHJEM = new Bransje(
            Bransjetype.SYKEHJEM,
            "Sykehjem",
            "87101", "87102"
    );
    private final static Bransje TRANSPORT = new Bransje(
            Bransjetype.TRANSPORT,
            "Rutebuss og persontrafikk (transport)",
            "49100", "49311", "49391", "49392"
    );

    private final static List<Bransje> bransjer = Arrays.asList(BARNEHAGER, NÆRINGSMIDDELINDUSTRI, SYKEHUS, SYKEHJEM, TRANSPORT);

    public Optional<Bransje> finnBransje(Underenhet underenhet) {
        return bransjer.stream()
                .filter(bransje -> bransje.inkludererVirksomhet(underenhet))
                .findAny();
    }
}
