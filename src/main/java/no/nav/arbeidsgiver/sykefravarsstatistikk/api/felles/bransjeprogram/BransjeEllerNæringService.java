package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram;

import java.util.List;
import java.util.Optional;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import org.springframework.stereotype.Component;

@Component
public class BransjeEllerNæringService {

  private final Bransjeprogram bransjeprogram;
  private final KlassifikasjonerRepository klassifikasjonerRepository;

  public BransjeEllerNæringService(
      Bransjeprogram bransjeprogram, KlassifikasjonerRepository klassifikasjonerRepository) {
    this.bransjeprogram = bransjeprogram;
    this.klassifikasjonerRepository = klassifikasjonerRepository;
  }

  @Deprecated
  public BransjeEllerNæring bestemFraNæringskode(Næringskode5Siffer næringskode5Siffer) {
    Optional<Bransje> bransje = bransjeprogram.finnBransje(næringskode5Siffer);

    boolean skalHenteDataPåNæring = bransje.isEmpty() || bransje.get().erDefinertPåTosiffernivå();

    if (skalHenteDataPåNæring) {
      return new BransjeEllerNæring(
          klassifikasjonerRepository.hentNæring(næringskode5Siffer.hentNæringskode2Siffer()));
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
            virksomhet.getNæringskode().hentNæringskode2Siffer()));
  }

  public BransjeEllerNæring finnBransjeFraMetadata(
      VirksomhetMetadata virksomhetMetaData, List<Næring> alleNæringer) {
    Underenhet virksomhet =
        new Underenhet(
            new Orgnr(virksomhetMetaData.getOrgnr()),
            new Orgnr(""),
            virksomhetMetaData.getNavn(),
            new Næringskode5Siffer(
                virksomhetMetaData.getNæringOgNæringskode5siffer().stream().findFirst().isPresent()
                    ? virksomhetMetaData.getNæringOgNæringskode5siffer().stream()
                        .findFirst()
                        .get()
                        .getNæringskode5Siffer()
                    : "00000",
                virksomhetMetaData.getNæringOgNæringskode5siffer().stream().findFirst().isPresent()
                    ? virksomhetMetaData.getNæringOgNæringskode5siffer().stream()
                        .findFirst()
                        .get()
                        .getNæring()
                    : ""),
            0);

    Optional<Bransje> bransje = bransjeprogram.finnBransje(virksomhet);

    if (bransje.isPresent()) {
      return new BransjeEllerNæring(bransje.get());
    }
    return new BransjeEllerNæring(hentNæringForVirksomhet(virksomhetMetaData, alleNæringer));
  }

  private Næring hentNæringForVirksomhet(
      VirksomhetMetadata virksomhetMetadata, List<Næring> næringer) {
    return næringer.stream()
        .filter(næring -> næring.getKode().equals(virksomhetMetadata.getNæring()))
        .findFirst()
        .orElse(new Næring(virksomhetMetadata.getNæring(), "Ukjent næring"));
  }
}
