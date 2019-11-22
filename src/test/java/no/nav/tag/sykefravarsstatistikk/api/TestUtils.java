package no.nav.tag.sykefravarsstatistikk.api;

import no.nav.tag.sykefravarsstatistikk.api.altinn.AltinnOrganisasjon;
import no.nav.tag.sykefravarsstatistikk.api.domene.Fnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.InnloggetBruker;
import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Næringskode5Siffer;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;

import java.math.BigDecimal;
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

    public static Orgnr getOrgnr() {
        return new Orgnr("971800534");
    }


    public static Underenhet enUnderenhet() {
        return enUnderenhetBuilder().orgnr(getOrgnr()).build();
    }

    public static Underenhet enUnderenhet(String orgnr) {
        return enUnderenhetBuilder().orgnr(new Orgnr(orgnr)).build();
    }

    public static Underenhet.UnderenhetBuilder enUnderenhetBuilder() {
        return Underenhet.builder()
                .orgnr(getOrgnr())
                .overordnetEnhetOrgnr(new Orgnr("053497180"))
                .navn("Underenhet AS")
                .næringskode(enNæringskode5Siffer())
                .antallAnsatte(40);

    }

    public static Næring enNæring() {
        return enNæring("12");
    }

    public static Næring enNæring(String kode) {
        return new Næring(kode, "en næring");
    }

    public static Næringskode5Siffer enNæringskode5Siffer() {
        return enNæringskode5Siffer("12345");
    }

    public static Næringskode5Siffer enNæringskode5Siffer(String kode) {
        return new Næringskode5Siffer(kode, "Spesiell næring");
    }

    public static Sykefraværprosent enSykefraværprosent(String label, int tapteDagsverk, int muligeDagsverk) {
        return new Sykefraværprosent(label, new BigDecimal(tapteDagsverk), new BigDecimal(muligeDagsverk), 40);
    }

    public static Sektor enSektor() {
        return new Sektor("1", "Statlig forvaltning");
    }
}
