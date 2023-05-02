package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import java.util.Arrays;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Fnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.InstitusjonellSektorkode;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.altinn.AltinnOrganisasjon;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.InnloggetBruker;

public class TestData {

  public static final String ORGNR_VIRKSOMHET_1 = "987654321";
  public static final String ORGNR_VIRKSOMHET_2 = "999999999";
  public static final String ORGNR_VIRKSOMHET_3 = "999999777";

  public static final String NÆRINGSKODE_5SIFFER = "10062";
  public static final String NÆRINGSKODE_2SIFFER = "10";
  public static final String SEKTOR = "3";

  public static InnloggetBruker getInnloggetBruker() {
    return getInnloggetBruker(getFnr().getVerdi());
  }

  public static InnloggetBruker getInnloggetBruker(String fnr) {
    InnloggetBruker bruker = new InnloggetBruker(new Fnr(fnr));
    bruker.setBrukerensOrganisasjoner(
        Arrays.asList(getOrganisasjon("999999999"), getOrganisasjon("111111111")));
    return bruker;
  }

  public static AltinnOrganisasjon getOrganisasjon(String organizationNumber) {
    return new AltinnOrganisasjon(null, null, null, organizationNumber, null, null);
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

  public static Underenhet enUnderenhet(String orgnr) {
    return new Underenhet(
        new Orgnr(orgnr), new Orgnr("053497180"), "Underenhet AS", enNæringskode5Siffer(), 40);
  }

  public static Underenhet.Builder enUnderenhetBuilder() {
    return Underenhet.builder()
        .orgnr(etOrgnr())
        .overordnetEnhetOrgnr(new Orgnr("053497180"))
        .navn("Underenhet AS")
        .næringskode(enNæringskode5Siffer())
        .antallAnsatte(40);
  }

  public static Næringskode5Siffer enNæringskode5Siffer() {
    return enNæringskode5Siffer("12345");
  }

  public static Næringskode5Siffer enNæringskode5Siffer(String kode) {
    return new Næringskode5Siffer(kode, "Spesiell næring");
  }

  public static ÅrstallOgKvartal etÅrstallOgKvartal() {
    return new ÅrstallOgKvartal(2019, 4);
  }
}
