package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BransjeEllerNæringService {
    private final Bransjeprogram bransjeprogram;
    private final KlassifikasjonerRepository klassifikasjonerRepository;

    public BransjeEllerNæringService(Bransjeprogram bransjeprogram, KlassifikasjonerRepository klassifikasjonerRepository) {
        this.bransjeprogram = bransjeprogram;
        this.klassifikasjonerRepository = klassifikasjonerRepository;
    }

    public BransjeEllerNæring getBransjeEllerNæring(Næringskode5Siffer næringskode5Siffer) {
        Optional<Bransje> bransje = bransjeprogram.finnBransje(næringskode5Siffer);

        boolean skalHenteDataPåNæring2Siffer =
                bransje.isEmpty()
                        || bransje.get().lengdePåNæringskoder() == 2;

        if (skalHenteDataPåNæring2Siffer) {
            Næring næring = klassifikasjonerRepository.hentNæring(næringskode5Siffer.hentNæringskode2Siffer());
            return new BransjeEllerNæring(Optional.empty(), Optional.of(næring));
        }

        return new BransjeEllerNæring(bransje, Optional.empty());
    }
}
