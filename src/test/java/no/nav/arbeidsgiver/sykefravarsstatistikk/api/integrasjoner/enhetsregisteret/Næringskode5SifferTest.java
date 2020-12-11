package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Næringskode5SifferTest {
    @Test
    public void næringskode__skal_fjerne_punktum() {
        Næringskode5Siffer kode = new Næringskode5Siffer("12.345", "");
        assertThat(kode.getKode()).isEqualTo("12345");
    }

    @Test
    public void næringskode__skal_validere_gyldig_kode() {
        Næringskode5Siffer kode = new Næringskode5Siffer("12345", "");
        assertThat(kode.getKode()).isEqualTo("12345");
    }

    @Test
    public void næringskode__skal_ikke_ha_mer_enn_5_siffer() {
        assertThrows(RuntimeException.class, () -> new Næringskode5Siffer("123456", ""));
    }

    @Test
    public void næringskode__skal_ikke_ha_mindre_enn_5_siffer() {
        assertThrows(RuntimeException.class, () -> new Næringskode5Siffer("1234", ""));
    }

    @Test
    public void næringskode__skal_ikke_være_null() {
        assertThrows(RuntimeException.class, () -> new Næringskode5Siffer(null, ""));
    }

    @Test
    public void næringskode__skal_kun_inneholde_tall() {
        assertThrows(RuntimeException.class, () -> new Næringskode5Siffer("1234a", ""));
    }

    @Test
    public void hentNæringskode2Siffer_skal_hente_de_to_første_sifrene() {
        Næringskode5Siffer næringskode = new Næringskode5Siffer("12345", "");
        assertThat(næringskode.hentNæringskode2Siffer()).isEqualTo("12");
    }
}
