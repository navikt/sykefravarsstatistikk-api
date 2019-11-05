package no.nav.tag.sykefravarsstatistikk.api;

import no.nav.tag.sykefravarsstatistikk.api.altinn.AltinnOrganisasjon;
import no.nav.tag.sykefravarsstatistikk.api.domene.Fnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.InnloggetBruker;

import java.util.Arrays;

public class TestUtils {

    public static InnloggetBruker getInnloggetBruker() {
        return getInnloggetBruker(getFnr().getVerdi());
    }

    public static InnloggetBruker getInnloggetBruker(String fnr) {
        InnloggetBruker bruker = new InnloggetBruker(new Fnr(fnr));
        bruker.setOrganisasjoner(Arrays.asList(
                getOrganisasjon("999999999"),
                getOrganisasjon("111111111")
        ));
        return bruker;
    }

    public static AltinnOrganisasjon getOrganisasjon(String organizationNumber) {
        AltinnOrganisasjon organisasjon = new AltinnOrganisasjon();
        organisasjon.setOrganizationNumber(organizationNumber);
        return organisasjon;
    }

    public static Fnr getFnr() {
        return new Fnr("26070248114");
    }
}
