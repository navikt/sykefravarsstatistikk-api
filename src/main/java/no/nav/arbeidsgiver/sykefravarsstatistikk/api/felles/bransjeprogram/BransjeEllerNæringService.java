package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram;

import java.util.List;
import java.util.Optional;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.VirksomhetMetadata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import org.springframework.stereotype.Component;

@Component
public class BransjeEllerNæringService {

  private final KlassifikasjonerRepository klassifikasjonerRepository;

  public BransjeEllerNæringService(KlassifikasjonerRepository klassifikasjonerRepository) {
    this.klassifikasjonerRepository = klassifikasjonerRepository;
  }

  @Deprecated
  public BransjeEllerNæring bestemFraNæringskode(Næringskode5Siffer næringskode5Siffer) {
    Optional<Bransje> bransje = Bransjeprogram.finnBransje(næringskode5Siffer);

    boolean skalHenteDataPåNæring = bransje.isEmpty() || bransje.get().erDefinertPåTosiffernivå();

    if (skalHenteDataPåNæring) {
      return new BransjeEllerNæring(
          klassifikasjonerRepository.hentNæring(næringskode5Siffer.hentNæringskode2Siffer()));
    } else {
      return new BransjeEllerNæring(bransje.get());
    }
  }

  public BransjeEllerNæring finnBransje(Virksomhet virksomhet) {
    Optional<Bransje> bransje = Bransjeprogram.finnBransje(virksomhet);

    return bransje
        .map(BransjeEllerNæring::new)
        .orElseGet(
            () ->
                new BransjeEllerNæring(
                    klassifikasjonerRepository.hentNæring(
                        virksomhet.getNæringskode().hentNæringskode2Siffer())));
  }

  public BransjeEllerNæring finnBransjeFraMetadata(
      VirksomhetMetadata virksomhetMetaData, List<Næring> alleNæringer) {
    UnderenhetLegacy virksomhet =
        new UnderenhetLegacy(
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

    Optional<Bransje> bransje = Bransjeprogram.finnBransje(virksomhet);

    return bransje
        .map(BransjeEllerNæring::new)
        .orElseGet(
            () ->
                new BransjeEllerNæring(hentNæringForVirksomhet(virksomhetMetaData, alleNæringer)));
  }

  private Næring hentNæringForVirksomhet(
      VirksomhetMetadata virksomhetMetadata, List<Næring> næringer) {
    return næringer.stream()
        .filter(næring -> næring.getKode().equals(virksomhetMetadata.getPrimærnæring()))
        .findFirst()
        .orElse(new Næring(virksomhetMetadata.getPrimærnæring(), "Ukjent næring"));
  }
}
