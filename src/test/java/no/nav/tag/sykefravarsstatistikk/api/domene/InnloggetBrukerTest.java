package no.nav.tag.sykefravarsstatistikk.api.domene;

import no.nav.tag.sykefravarsstatistikk.api.TestUtils;
import no.nav.tag.sykefravarsstatistikk.api.altinn.AltinnOrganisasjon;
import no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollException;
import org.junit.Test;

import java.util.List;

import static no.nav.tag.sykefravarsstatistikk.api.TestUtils.getInnloggetBruker;

public class InnloggetBrukerTest {

    @Test
    public void sjekkTilgang__kaster_ingen_Exception_hvis_bruker_har_tilgang_til_orgnr() {
        InnloggetBruker bruker = getInnloggetBruker("12345678901");

        AltinnOrganisasjon organisasjon = TestUtils.getOrganisasjon("123456789");
        bruker.setOrganisasjoner(List.of(organisasjon));

        bruker.sjekkTilgang(new Orgnr(organisasjon.getOrganizationNumber()));
    }

    @Test(expected = TilgangskontrollException.class)
    public void sjekkTilgang__skal_feile_hvis_bruker_ikke_har_tilgang_til_noen_org() {
        InnloggetBruker bruker = getInnloggetBruker("12345678901");

        bruker.sjekkTilgang(new Orgnr("123456789"));
    }

    @Test(expected = TilgangskontrollException.class)
    public void sjekkTilgang__skal_feile_hvis_bruker_ikke_har_tilgang_til_orgnr() {
        InnloggetBruker bruker = getInnloggetBruker("12345678901");

        AltinnOrganisasjon organisasjon = TestUtils.getOrganisasjon("123456789");
        bruker.setOrganisasjoner(List.of(organisasjon));

        bruker.sjekkTilgang(new Orgnr("987654321"));
    }

    @Test
    public void sjekkTilgang__skal_ikke_feile_selv_om_listen_med_organisasjoner_har_null() {
        InnloggetBruker bruker = getInnloggetBruker("12345678901");

        List<AltinnOrganisasjon> organisasjoner = List.of(
                TestUtils.getOrganisasjon(null),
                TestUtils.getOrganisasjon("987654321")
        );

        bruker.setOrganisasjoner(organisasjoner);

        bruker.sjekkTilgang(new Orgnr("987654321"));
    }

}