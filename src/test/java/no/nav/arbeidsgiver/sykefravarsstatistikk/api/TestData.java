package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Fnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.InstitusjonellSektorkode;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.OverordnetEnhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjetype;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.virksomhetsklassifikasjoner.Næringsgruppering;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.altinn.AltinnOrganisasjon;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.besøksstatistikk.SammenligningEvent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.besøksstatistikk.sammenligning.Sammenligning;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.besøksstatistikk.sammenligning.Sykefraværprosent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.InnloggetBruker;

import java.math.BigDecimal;
import java.util.Arrays;

public class TestData {

    public static final String ORGNR_VIRKSOMHET_1 = "987654321";
    public static final String ORGNR_VIRKSOMHET_2 = "999999999";
    public static final String ORGNR_VIRKSOMHET_3 = "999999777";

    public static final String NÆRINGSKODE_5SIFFER = "10062";
    public static final String NÆRINGSKODE_2SIFFER = "10";

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

    public static Næringsgruppering enNæringsgruppering(String kode5siffer) {
        return new Næringsgruppering(
                kode5siffer,
                "Test5",
                kode5siffer.substring(0, 4),
                "test4",
                kode5siffer.substring(0, 3),
                "test3",
                kode5siffer.substring(0, 2),
                "test2",
                "02",
                "test1"
        );
    }

    public static Næringsgruppering enNæringsgruppering() {
        return enNæringsgruppering("02123");
    }

    public static AltinnOrganisasjon getOrganisasjon(String organizationNumber) {
        AltinnOrganisasjon organisasjon = new AltinnOrganisasjon();
        organisasjon.setOrganizationNumber(organizationNumber);
        return organisasjon;
    }

    public static SammenligningEvent.SammenligningEventBuilder enSammenligningEventBuilder() {
        return SammenligningEvent.builder()
                .underenhet(enUnderenhet())
                .overordnetEnhet(enEnhet())
                .ssbSektor(enSektor())
                .næring5siffer(enNæringskode5Siffer())
                .næring2siffer(enNæring())
                .sammenligning(enSammenligning())
                .sessionId("sessionId");
    }

    public static Sammenligning enSammenligning() {
        return enSammenligningBuilder().build();
    }

    public static Sammenligning.SammenligningBuilder enSammenligningBuilder() {
        return Sammenligning.builder()
                .kvartal(2)
                .årstall(2019)
                .virksomhet(enSykefraværprosent("Virksomhet AS", 100, 6000, 100))
                .næring(enSykefraværprosent("Næring", 1100, 60000, 1000))
                .bransje(null)
                .sektor(enSykefraværprosent("Sektor", 11100, 600000, 10000))
                .land(enSykefraværprosent("Land", 111100, 6000000, 100000));
    }

    public static Fnr getFnr() {
        return new Fnr("26070248114");
    }

    public static Orgnr etOrgnr() {
        return new Orgnr("971800534");
    }

    public static InstitusjonellSektorkode enInstitusjonellSektorkode() {
        return new InstitusjonellSektorkode("1234", "sektor!");
    }

    public static OverordnetEnhet enEnhet() {
        return OverordnetEnhet.builder()
                .orgnr(etOrgnr())
                .antallAnsatte(10)
                .navn("Enhet AS")
                .institusjonellSektorkode(enInstitusjonellSektorkode())
                .næringskode(enNæringskode5Siffer())
                .build();
    }

    public static Underenhet enUnderenhet() {
        return enUnderenhetBuilder().orgnr(etOrgnr()).build();
    }

    public static Underenhet enUnderenhet(String orgnr) {
        return enUnderenhetBuilder().orgnr(new Orgnr(orgnr)).build();
    }

    public static Underenhet.UnderenhetBuilder enUnderenhetBuilder() {
        return Underenhet.builder()
                .orgnr(etOrgnr())
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

    public static Sykefraværprosent enSykefraværprosent(String label, int tapteDagsverk, int muligeDagsverk, int antallPersoner) {
        return new Sykefraværprosent(label, new BigDecimal(tapteDagsverk), new BigDecimal(muligeDagsverk), antallPersoner);
    }

    public static Sykefraværprosent enSykefraværprosent(int antallAnsatte) {
        return new Sykefraværprosent("Hei AS", new BigDecimal(5), new BigDecimal(100), antallAnsatte);
    }

    public static Sykefraværprosent enSykefraværprosent() {
        return enSykefraværprosent(8);
    }

    public static Sektor enSektor() {
        return new Sektor("1", "Statlig forvaltning");
    }

    public static Bransje enBransje() {
        return new Bransje(Bransjetype.SYKEHUS, "bransje", "12322");
    }

    public static ÅrstallOgKvartal etÅrstallOgKvartal() {
        return new ÅrstallOgKvartal(2019, 4);
    }
}
