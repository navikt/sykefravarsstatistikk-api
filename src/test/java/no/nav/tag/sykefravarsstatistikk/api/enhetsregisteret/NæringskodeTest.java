package no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret;

import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class NæringskodeTest {
    @Test
    public void næringskode__skal_fjerne_punktum() {
        Næringskode kode = new Næringskode("12.345", "");
        assertThat(kode.getKode()).isEqualTo("12345");
    }

    @Test
    public void næringskode__skal_validere_gyldig_kode() {
        Næringskode kode = new Næringskode("12345", "");
        assertThat(kode.getKode()).isEqualTo("12345");
    }

    @Test(expected = RuntimeException.class)
    public void næringskode__skal_ikke_ha_mer_enn_5_siffer() {
        new Næringskode("123456", "");
    }

    @Test(expected = RuntimeException.class)
    public void næringskode__skal_ikke_ha_mindre_enn_5_siffer() {
        new Næringskode("1234", "");
    }

    @Test(expected = RuntimeException.class)
    public void næringskode__skal_kun_inneholde_tall() {
        new Næringskode("1234a", "");
    }
}