package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BransjeEllerNæringService {

    private final Bransjeprogram bransjeprogram;
    private final KlassifikasjonerRepository klassifikasjonerRepository;


    public BransjeEllerNæringService(Bransjeprogram bransjeprogram,
            KlassifikasjonerRepository klassifikasjonerRepository) {
        this.bransjeprogram = bransjeprogram;
        this.klassifikasjonerRepository = klassifikasjonerRepository;
    }


    @Deprecated
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


    public BransjeEllerNæring finnBransje(Underenhet virksomhet) {
        Optional<Bransje> bransje = bransjeprogram.finnBransje(virksomhet);

        if (bransje.isPresent()) {
            return new BransjeEllerNæring(bransje.get());
        }
        return new BransjeEllerNæring(
                klassifikasjonerRepository.hentNæring(
                        virksomhet.getNæringskode().hentNæringskode2Siffer())
        );
    }

    public BransjeEllerNæring finnBransjeFraMetadata(VirksomhetMetadata virksomhetMetaData) {
        Underenhet virksomhet = new Underenhet(
              new Orgnr(virksomhetMetaData.getOrgnr()),
              new Orgnr(""),
              virksomhetMetaData.getNavn(),
              new Næringskode5Siffer(
                    virksomhetMetaData.getNæringOgNæringskode5siffer().stream().findFirst().isPresent()
                          ? virksomhetMetaData.getNæringOgNæringskode5siffer().stream().findFirst().get().getNæringskode5Siffer()
                          : "00000",
                    virksomhetMetaData.getNæringOgNæringskode5siffer().stream().findFirst().isPresent()
                          ? virksomhetMetaData.getNæringOgNæringskode5siffer().stream().findFirst().get().getNæring()
                          : ""
              )
              ,

              0
        );

        Optional<Bransje> bransje = bransjeprogram.finnBransje(virksomhet);

        if (bransje.isPresent()) {
            return new BransjeEllerNæring(bransje.get());
        }
        return new BransjeEllerNæring(
                klassifikasjonerRepository.hentNæring(
                        virksomhet.getNæringskode().hentNæringskode2Siffer())
        );
    }
}
