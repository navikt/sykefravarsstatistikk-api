package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram;

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

    public BransjeEllerNæring bestemFraNæringskode(Næringskode5Siffer næringskode5Siffer) {
        Optional<Bransje> bransje = bransjeprogram.finnBransje(næringskode5Siffer);

        boolean skalHenteDataPåNæring2Siffer =
                bransje.isEmpty()
                        || bransje.get().erDefinertPåTosiffernivå();

        if (skalHenteDataPåNæring2Siffer) {
            return new BransjeEllerNæring(
                    klassifikasjonerRepository.hentNæring(
                            næringskode5Siffer.hentNæringskode2Siffer())
            );
        } else {
            return new BransjeEllerNæring(bransje.get());
        }
    }
}
