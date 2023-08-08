package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.bransjeprogram;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.UnderenhetLegacy;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.bransjeprogram.ArbeidsmiljøportalenBransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.bransjeprogram.Bransjeprogram;
import org.junit.jupiter.api.Test;

public class BransjeTest {
  @Test
  public void
      virksomhetTilhørerBransjeprogram__skal_gi_true_hvis_næringskode_starter_med_de_definerte_sifrene() {
    Bransje bransje = new Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "test", "12", "45");
    assertThat(bransje.inkludererVirksomhet(underenhetMedNæringskode("45512"))).isTrue();
  }

  @Test
  public void
      virksomhetTilhørerBransjeprogram__skal_gi_false_hvis_næringskode_ikke_starter_med_de_definerte_sifrene() {
    Bransje bransje = new Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "test", "12", "45");
    assertThat(bransje.inkludererVirksomhet(underenhetMedNæringskode("46512"))).isFalse();
  }

  @Test
  public void finnBransje__mapperBarnehageTilBarnehagebransjen() {
    Næringskode5Siffer enBarnehage = new Næringskode5Siffer("88911", "");
    Bransje barnehageBransjen = Bransjeprogram.finnBransje(enBarnehage).orElseThrow();

    assertThat(barnehageBransjen.getNavn()).isEqualTo("Barnehager");
  }

  @Test
  public void finnBransje__mapperBoligbyggelagTilByggebransjen() {
    Næringskode5Siffer etBoligbyggerlag = new Næringskode5Siffer("41101", "");
    Bransje byggebranasjen = Bransjeprogram.finnBransje(etBoligbyggerlag).orElseThrow();

    assertThat(byggebranasjen.getNavn()).isEqualTo("Bygg");
  }

  @Test
  public void finnBransje__mapperBroOgTunellbyggerTilAnleggsbransjen() {
    Næringskode5Siffer enByggerAvBroOgTunnel = new Næringskode5Siffer("42130", "");

    Bransje byggebransjen = Bransjeprogram.finnBransje(enByggerAvBroOgTunnel).orElseThrow();
    assertThat(byggebransjen.getNavn()).isEqualTo("Anlegg");
  }

  @Test
  public void finnBransje__mapperProdusentAvIskremTilNæringsmiddelindustrien() {
    Næringskode5Siffer enProdusentAvIskrem = new Næringskode5Siffer("10520", "");

    Bransje næringsmiddelindustrien = Bransjeprogram.finnBransje(enProdusentAvIskrem).orElseThrow();
    assertThat(næringsmiddelindustrien.getNavn()).isEqualTo("Næringsmiddelsindustrien");
  }

  @Test
  public void finnBransje__mapperSomatiskeSpesialsykehusTilSykehusbransjen() {
    Næringskode5Siffer etSomatiskSpesialsykehus = new Næringskode5Siffer("86102", "");

    Bransje sykehusbransjen = Bransjeprogram.finnBransje(etSomatiskSpesialsykehus).orElseThrow();
    assertThat(sykehusbransjen.getNavn()).isEqualTo("Sykehus");
  }

  @Test
  public void finnBransje__mapperSykehjemTilSykehjemsbransjen() {
    Næringskode5Siffer etSomatiskSykehjem = new Næringskode5Siffer("87102", "");

    Bransje sykehjemsbransjen = Bransjeprogram.finnBransje(etSomatiskSykehjem).orElseThrow();
    assertThat(sykehjemsbransjen.getNavn()).isEqualTo("Sykehjem");
  }

  @Test
  public void finnBransje__mapperTurbiltransportTilTransportbransjen() {
    Næringskode5Siffer enturbiltransportør = new Næringskode5Siffer("49392", "");

    Bransje sykehjemsbransjen = Bransjeprogram.finnBransje(enturbiltransportør).orElseThrow();
    assertThat(sykehjemsbransjen.getNavn()).isEqualTo("Rutebuss og persontrafikk (transport)");
  }

  @Test
  public void bransje__skal_ikke_godta_koder_med_lengde_utenom_5_og_2() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "navn", "123"));
  }

  @Test
  public void bransje__skal_ikke_godta_koder_med_forskjellige_lengder() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "navn", "12", "12345"));
  }

  @Test
  public void bransje__skal_godta_koder_med_lengde_2_eller_5() {
    new Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "navn", "12", "34", "56");
    new Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "navn", "12345", "34567", "56789");
  }

  @Test
  public void lengdePåNæringskoder__skal_returnere_riktig_lengde() {
    assertTrue(
        new Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "navn", "12345", "34567", "56789")
            .erDefinertPåFemsiffernivå());
    assertTrue(
        new Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "navn", "12", "34", "45")
            .erDefinertPåTosiffernivå());
  }

  @Test
  public void inkludererVirksomhet__skal_returnere_hvorvidt_virksomhetens_næring_er_i_bransjen() {
    Bransje bransje =
        new Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "navn", "12345", "34567", "56789");
    assertThat(bransje.inkludererVirksomhet(underenhetMedNæringskode("12345"))).isTrue();
    assertThat(bransje.inkludererVirksomhet(underenhetMedNæringskode("54321"))).isFalse();
  }

  private UnderenhetLegacy underenhetMedNæringskode(String næringskode) {
    return new UnderenhetLegacy(
        etOrgnr(), new Orgnr("053497180"), "Underenhet AS", enNæringskode5Siffer(næringskode), 40);
  }
}
