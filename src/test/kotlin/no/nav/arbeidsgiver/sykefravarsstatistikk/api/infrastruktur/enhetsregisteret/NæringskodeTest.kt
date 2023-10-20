package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Næringskode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class NæringskodeTest {
    @Test
    fun næringskode__skal_feile_hvis_kode_har_punktum() {
        Assertions.assertThrows(IllegalArgumentException::class.java) { Næringskode("52.292") }
    }

    @Test
    fun næringskode__skal_validere_gyldig_kode() {
        val (femsifferIdentifikator) = Næringskode("12345")
        org.assertj.core.api.Assertions.assertThat(femsifferIdentifikator).isEqualTo("12345")
    }

    @Test
    fun næringskode__skal_ikke_ha_mer_enn_5_siffer() {
        Assertions.assertThrows(RuntimeException::class.java) { Næringskode("123456") }
    }

    @Test
    fun næringskode__skal_ikke_ha_mindre_enn_5_siffer() {
        Assertions.assertThrows(RuntimeException::class.java) { Næringskode("1234") }
    }

    @Test
    fun næringskode__skal_kun_inneholde_tall() {
        Assertions.assertThrows(RuntimeException::class.java) { Næringskode("1234a") }
    }

    @Test
    fun hentNæringskode2Siffer_skal_hente_de_to_første_sifrene() {
        val næringskode = Næringskode("12345")
        org.assertj.core.api.Assertions.assertThat(næringskode.næring.tosifferIdentifikator).isEqualTo("12")
    }
}
