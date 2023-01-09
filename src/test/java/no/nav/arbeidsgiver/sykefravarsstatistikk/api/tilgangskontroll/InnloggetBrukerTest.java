package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.altinn.AltinnOrganisasjon;
import org.junit.jupiter.api.Test;

import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.getInnloggetBruker;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InnloggetBrukerTest {

  @Test
  public void sjekkTilgang__kaster_ingen_Exception_hvis_bruker_har_tilgang_til_orgnr() {
    InnloggetBruker bruker = getInnloggetBruker("12345678901");

    AltinnOrganisasjon organisasjon = TestData.getOrganisasjon("123456789");
    bruker.setOrganisasjoner(List.of(organisasjon));

    bruker.sjekkTilgang(new Orgnr(organisasjon.getOrganizationNumber()));
  }

  @Test
  public void sjekkTilgang__skal_feile_hvis_bruker_ikke_har_tilgang_til_noen_org() {
    InnloggetBruker bruker = getInnloggetBruker("12345678901");

    assertThrows(
        TilgangskontrollException.class, () -> bruker.sjekkTilgang(new Orgnr("123456789")));
  }

  @Test
  public void sjekkTilgang__skal_feile_hvis_bruker_ikke_har_tilgang_til_orgnr() {
    InnloggetBruker bruker = getInnloggetBruker("12345678901");

    AltinnOrganisasjon organisasjon = TestData.getOrganisasjon("123456789");
    bruker.setOrganisasjoner(List.of(organisasjon));

    assertThrows(
        TilgangskontrollException.class, () -> bruker.sjekkTilgang(new Orgnr("987654321")));
  }

  @Test
  public void sjekkTilgang__skal_ikke_feile_selv_om_listen_med_organisasjoner_har_null() {
    InnloggetBruker bruker = getInnloggetBruker("12345678901");

    List<AltinnOrganisasjon> organisasjoner =
        List.of(TestData.getOrganisasjon(null), TestData.getOrganisasjon("987654321"));

    bruker.setOrganisasjoner(organisasjoner);

    bruker.sjekkTilgang(new Orgnr("987654321"));
  }
}
