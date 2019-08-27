package no.nav.tag.sykefravarsstatistikk.api;

import no.nav.tag.sykefravarsstatistikk.api.altinn.Organisasjon;
import no.nav.tag.sykefravarsstatistikk.api.domain.Fnr;
import no.nav.tag.sykefravarsstatistikk.api.domain.autorisasjon.InnloggetSelvbetjeningBruker;

public class TestUtils {


    public static InnloggetSelvbetjeningBruker getInnloggetSelvbetjeningBruker(String fnr) {
        return new InnloggetSelvbetjeningBruker(new Fnr(fnr));
    }

    public static Organisasjon getOrganisasjon(String organizationNumber) {
        Organisasjon organisasjon = new Organisasjon();
        organisasjon.setOrganizationNumber(organizationNumber);
        return organisasjon;
    }

}
