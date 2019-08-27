package no.nav.tag.sykefravarsstatistikk.api.domain.autorisasjon;

import no.nav.tag.sykefravarsstatistikk.api.TestUtils;
import no.nav.tag.sykefravarsstatistikk.api.altinn.Organisasjon;
import no.nav.tag.sykefravarsstatistikk.api.domain.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollException;
import org.junit.Test;

import java.util.List;

import static no.nav.tag.sykefravarsstatistikk.api.TestUtils.getInnloggetSelvbetjeningBruker;

public class InnloggetSelvbetjeningBrukerTest {

    @Test
    public void sjekkTilgang__kaster_ingen_Exception_hvis_bruker_har_tilgang_til_orgnr() {
        InnloggetSelvbetjeningBruker bruker = getInnloggetSelvbetjeningBruker("12345678901");

        Organisasjon organisasjon = TestUtils.getOrganisasjon("123456789");
        bruker.setOrganisasjoner(List.of(organisasjon));

        bruker.sjekkTilgang(new Orgnr(organisasjon.getOrganizationNumber()));
    }

    @Test(expected = TilgangskontrollException.class)
    public void sjekkTilgang__skal_feile_hvis_bruker_ikke_har_tilgang_til_noen_org() {
        InnloggetSelvbetjeningBruker bruker = getInnloggetSelvbetjeningBruker("12345678901");

        bruker.sjekkTilgang(new Orgnr("123456789"));
    }

    @Test(expected = TilgangskontrollException.class)
    public void sjekkTilgang__skal_feile_hvis_bruker_ikke_har_tilgang_til_orgnr() {
        InnloggetSelvbetjeningBruker bruker = getInnloggetSelvbetjeningBruker("12345678901");

        Organisasjon organisasjon = TestUtils.getOrganisasjon("123456789");
        bruker.setOrganisasjoner(List.of(organisasjon));

        bruker.sjekkTilgang(new Orgnr("987654321"));
    }

}