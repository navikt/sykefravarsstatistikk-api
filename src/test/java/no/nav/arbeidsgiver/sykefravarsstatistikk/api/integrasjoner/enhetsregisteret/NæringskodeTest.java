package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næringskode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NæringskodeTest {
  @Test
  public void næringskode__skal_feile_hvis_kode_har_punktum() {
    assertThrows(IllegalArgumentException.class, () ->  new Næringskode("52.292"));
  }

  @Test
  public void næringskode__skal_validere_gyldig_kode() {
    Næringskode næringskode = new Næringskode("12345");
    assertThat(næringskode.getFemsifferIdentifikator()).isEqualTo("12345");
  }

  @Test
  public void næringskode__skal_ikke_ha_mer_enn_5_siffer() {
    assertThrows(RuntimeException.class, () -> new Næringskode("123456"));
  }

  @Test
  public void næringskode__skal_ikke_ha_mindre_enn_5_siffer() {
    assertThrows(RuntimeException.class, () -> new Næringskode("1234"));
  }

  @Test
  public void næringskode__skal_ikke_være_null() {
    assertThrows(RuntimeException.class, () -> new Næringskode(null));
  }

  @Test
  public void næringskode__skal_kun_inneholde_tall() {
    assertThrows(RuntimeException.class, () -> new Næringskode("1234a"));
  }

  @Test
  public void hentNæringskode2Siffer_skal_hente_de_to_første_sifrene() {
    Næringskode næringskode = new Næringskode("12345");
    assertThat(næringskode.getNæring().getTosifferIdentifikator()).isEqualTo("12");
  }
}
