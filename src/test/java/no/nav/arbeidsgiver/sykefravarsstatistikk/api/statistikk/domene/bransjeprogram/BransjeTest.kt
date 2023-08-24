package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.bransjeprogram

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.enNæringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.etOrgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.BedreNæringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.UnderenhetLegacy
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.ArbeidsmiljøportalenBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransjeprogram.finnBransje
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class BransjeTest {
    @Test
    fun virksomhetTilhørerBransjeprogram__skal_gi_true_hvis_næringskode_starter_med_de_definerte_sifrene() {
        val bransje = Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "test", listOf("12", "45"))
        Assertions.assertThat(bransje.inkludererVirksomhet(underenhetMedNæringskode("45512"))).isTrue()
    }

    @Test
    fun virksomhetTilhørerBransjeprogram__skal_gi_false_hvis_næringskode_ikke_starter_med_de_definerte_sifrene() {
        val bransje = Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "test", listOf("12", "45"))
        Assertions.assertThat(bransje.inkludererVirksomhet(underenhetMedNæringskode("46512"))).isFalse()
    }

    @Test
    fun finnBransje__mapperBarnehageTilBarnehagebransjen() {
        val enBarnehage = BedreNæringskode("88911")
        val (_, navn) = finnBransje(enBarnehage).orElseThrow()
        Assertions.assertThat(navn).isEqualTo("Barnehager")
    }

    @Test
    fun finnBransje__mapperBoligbyggelagTilByggebransjen() {
        val etBoligbyggerlag = BedreNæringskode("41101")
        val (_, navn) = finnBransje(etBoligbyggerlag).orElseThrow()
        Assertions.assertThat(navn).isEqualTo("Bygg")
    }

    @Test
    fun finnBransje__mapperBroOgTunellbyggerTilAnleggsbransjen() {
        val enByggerAvBroOgTunnel = BedreNæringskode("42130")
        val (_, navn) = finnBransje(enByggerAvBroOgTunnel).orElseThrow()
        Assertions.assertThat(navn).isEqualTo("Anlegg")
    }

    @Test
    fun finnBransje__mapperProdusentAvIskremTilNæringsmiddelindustrien() {
        val enProdusentAvIskrem = BedreNæringskode("10520")
        val (_, navn) = finnBransje(enProdusentAvIskrem).orElseThrow()
        Assertions.assertThat(navn).isEqualTo("Næringsmiddelsindustrien")
    }

    @Test
    fun finnBransje__mapperSomatiskeSpesialsykehusTilSykehusbransjen() {
        val etSomatiskSpesialsykehus = BedreNæringskode("86102")
        val (_, navn) = finnBransje(etSomatiskSpesialsykehus).orElseThrow()
        Assertions.assertThat(navn).isEqualTo("Sykehus")
    }

    @Test
    fun finnBransje__mapperSykehjemTilSykehjemsbransjen() {
        val etSomatiskSykehjem = BedreNæringskode("87102")
        val (_, navn) = finnBransje(etSomatiskSykehjem).orElseThrow()
        Assertions.assertThat(navn).isEqualTo("Sykehjem")
    }

    @Test
    fun finnBransje__mapperTurbiltransportTilTransportbransjen() {
        val enturbiltransportør = BedreNæringskode("49392")
        val (_, navn) = finnBransje(enturbiltransportør).orElseThrow()
        Assertions.assertThat(navn).isEqualTo("Rutebuss og persontrafikk (transport)")
    }

    @Test
    fun bransje__skal_ikke_godta_koder_med_lengde_utenom_5_og_2() {
        org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException::class.java
        ) { Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "navn", listOf("123")) }
    }

    @Test
    fun bransje__skal_ikke_godta_koder_med_forskjellige_lengder() {
        org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException::class.java
        ) { Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "navn", listOf("12", "12345")) }
    }

    @Test
    fun bransje__skal_godta_koder_med_lengde_2_eller_5() {
        Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "navn", listOf("12", "34", "56"))
        Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "navn", listOf("12345", "34567", "56789"))
    }

    @Test
    fun lengdePåNæringskoder__skal_returnere_riktig_lengde() {
        org.junit.jupiter.api.Assertions.assertTrue(
            Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "navn", listOf("12345", "34567", "56789"))
                .erDefinertPåFemsiffernivå()
        )
        org.junit.jupiter.api.Assertions.assertTrue(
            Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "navn", listOf("12", "34", "45"))
                .erDefinertPåTosiffernivå()
        )
    }

    @Test
    fun inkludererVirksomhet__skal_returnere_hvorvidt_virksomhetens_næring_er_i_bransjen() {
        val bransje = Bransje(ArbeidsmiljøportalenBransje.SYKEHUS, "navn", listOf("12345", "34567", "56789"))
        Assertions.assertThat(bransje.inkludererVirksomhet(underenhetMedNæringskode("12345"))).isTrue()
        Assertions.assertThat(bransje.inkludererVirksomhet(underenhetMedNæringskode("00000"))).isFalse()
    }

    private fun underenhetMedNæringskode(næringskode: String): UnderenhetLegacy {
        return UnderenhetLegacy(
            etOrgnr(), Orgnr("053497180"), "Underenhet AS", enNæringskode(næringskode), 40
        )
    }
}
