package no.nav.tag.sykefravarsstatistikk.api;

import no.nav.tag.sykefravarsstatistikk.api.altinn.Organisasjon;
import no.nav.tag.sykefravarsstatistikk.api.domene.Fnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.InnloggetBruker;

public class TestUtils {


    public static InnloggetBruker getInnloggetSelvbetjeningBruker(String fnr) {
        return new InnloggetBruker(new Fnr(fnr));
    }

    public static Organisasjon getOrganisasjon(String organizationNumber) {
        Organisasjon organisasjon = new Organisasjon();
        organisasjon.setOrganizationNumber(organizationNumber);
        return organisasjon;
    }

}
