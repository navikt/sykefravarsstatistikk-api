package no.nav.tag.sykefravarsstatistikk.api;

import no.nav.tag.sykefravarsstatistikk.api.altinn.Organisasjon;
import no.nav.tag.sykefravarsstatistikk.api.domene.Fnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.InnloggetBruker;

import java.util.Arrays;

public class TestUtils {


    public static InnloggetBruker getInnloggetSelvbetjeningBruker(String fnr) {
        InnloggetBruker bruker = new InnloggetBruker(new Fnr(fnr));
        bruker.setOrganisasjoner(Arrays.asList(
                getOrganisasjon("999999999"),
                getOrganisasjon("111111111")
        ));
        return bruker;
    }

    public static Organisasjon getOrganisasjon(String organizationNumber) {
        Organisasjon organisasjon = new Organisasjon();
        organisasjon.setOrganizationNumber(organizationNumber);
        return organisasjon;
    }

}
