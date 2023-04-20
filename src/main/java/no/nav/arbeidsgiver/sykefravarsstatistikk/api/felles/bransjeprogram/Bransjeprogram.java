package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import org.springframework.stereotype.Component;

@Component
public class Bransjeprogram {

  public static final Bransje BARNEHAGER =
      new Bransje(ArbeidsmiljøportalenBransje.BARNEHAGER, "Barnehager", "88911");
  public static final Bransje NÆRINGSMIDDELINDUSTRI =
      new Bransje(
          ArbeidsmiljøportalenBransje.NÆRINGSMIDDELINDUSTRI, "Næringsmiddelsindustrien", "10");
  public static final Bransje SYKEHUS =
      new Bransje(
          ArbeidsmiljøportalenBransje.SYKEHUS,
          "Sykehus",
          "86101",
          "86102",
          "86104",
          "86105",
          "86106",
          "86107");
  public static final Bransje SYKEHJEM =
      new Bransje(ArbeidsmiljøportalenBransje.SYKEHJEM, "Sykehjem", "87101", "87102");
  public static final Bransje TRANSPORT =
      new Bransje(
          ArbeidsmiljøportalenBransje.TRANSPORT,
          "Rutebuss og persontrafikk (transport)",
          "49100",
          "49311",
          "49391",
          "49392");
  public static final Bransje BYGG = new Bransje(ArbeidsmiljøportalenBransje.BYGG, "Bygg", "41");
  public static final Bransje ANLEGG =
      new Bransje(ArbeidsmiljøportalenBransje.ANLEGG, "Anlegg", "42");

  public static final List<Bransje> bransjer =
      Arrays.asList(BARNEHAGER, NÆRINGSMIDDELINDUSTRI, SYKEHUS, SYKEHJEM, TRANSPORT, BYGG, ANLEGG);

  public Optional<Bransje> finnBransje(Underenhet underenhet) {
    return bransjer.stream().filter(bransje -> bransje.inkludererVirksomhet(underenhet)).findAny();
  }

  public Optional<Bransje> finnBransje(Næringskode5Siffer næringskode5Siffer) {
    return bransjer.stream()
        .filter(bransje -> bransje.inkludererNæringskode(næringskode5Siffer))
        .findAny();
  }
}
