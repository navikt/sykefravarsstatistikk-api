package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

import ia.felles.definisjoner.bransjer.BransjeId
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Bransjeprogram.finnBransje
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import testUtils.TestData.enNæringskode
import testUtils.TestData.etOrgnr
import ia.felles.definisjoner.bransjer.Bransje
class BransjeTest {
    private fun Bransje.inkludererVirksomhet(virksomhet: Virksomhet): Boolean {
        return bransjeId.let {
            when (it) {
                is BransjeId.Næring -> it.næring == virksomhet.næringskode.næring.tosifferIdentifikator
                is BransjeId.Næringskoder -> it.næringskoder.contains(virksomhet.næringskode.femsifferIdentifikator)
            }
        }
    }

    @Test
    fun virksomhetTilhørerBransjeprogram__skal_gi_true_hvis_næringskode_starter_med_de_definerte_sifrene() {
        Assertions.assertThat(Bransje.BYGG.inkludererVirksomhet(underenhetMedNæringskode("41999"))).isTrue()
    }

    @Test
    fun virksomhetTilhørerBransjeprogram__skal_gi_false_hvis_næringskode_ikke_starter_med_de_definerte_sifrene() {
        Assertions.assertThat(Bransje.SYKEHUS.inkludererVirksomhet(underenhetMedNæringskode("46512"))).isFalse()
    }

    @Test
    fun finnBransje__mapperBarnehageTilBarnehagebransjen() {
        val enBarnehage = Næringskode("88911")
        val navn = finnBransje(enBarnehage)?.navn
        Assertions.assertThat(navn).isEqualTo("Barnehager")
    }

    @Test
    fun finnBransje__mapperBoligbyggelagTilByggebransjen() {
        val etBoligbyggerlag = Næringskode("41101")
        val navn = finnBransje(etBoligbyggerlag)?.navn
        Assertions.assertThat(navn).isEqualTo("Bygg")
    }

    @Test
    fun finnBransje__mapperBroOgTunellbyggerTilAnleggsbransjen() {
        val enByggerAvBroOgTunnel = Næringskode("42130")
        val navn = finnBransje(enByggerAvBroOgTunnel)?.navn
        Assertions.assertThat(navn).isEqualTo("Anlegg")
    }

    @Test
    fun finnBransje__mapperProdusentAvIskremTilNæringsmiddelindustrien() {
        val enProdusentAvIskrem = Næringskode("10520")
        val navn = finnBransje(enProdusentAvIskrem)?.navn
        Assertions.assertThat(navn).isEqualTo("Næringsmiddelindustri")
    }

    @Test
    fun finnBransje__mapperSomatiskeSpesialsykehusTilSykehusbransjen() {
        val etSomatiskSpesialsykehus = Næringskode("86102")
        val navn = finnBransje(etSomatiskSpesialsykehus)?.navn
        Assertions.assertThat(navn).isEqualTo("Sykehus")
    }

    @Test
    fun finnBransje__mapperSykehjemTilSykehjemsbransjen() {
        val etSomatiskSykehjem = Næringskode("87102")
        val navn = finnBransje(etSomatiskSykehjem)?.navn
        Assertions.assertThat(navn).isEqualTo("Sykehjem")
    }

    @Test
    fun finnBransje__mapperTurbiltransportTilTransportbransjen() {
        val enturbiltransportør = Næringskode("49392")
        val navn = finnBransje(enturbiltransportør)?.navn
        Assertions.assertThat(navn).isEqualTo("Transport")
    }

    @Test
    fun lengdePåNæringskoder__skal_returnere_riktig_lengde() {
        assertTrue(
            Bransje.SYKEHUS.bransjeId is BransjeId.Næringskoder
        )
        assertTrue(
            Bransje.BYGG.bransjeId is BransjeId.Næring
        )
    }

    @Test
    fun inkludererVirksomhet__skal_returnere_hvorvidt_virksomhetens_næring_er_i_bransjen() {
        val bransje = Bransje.SYKEHUS
        Assertions.assertThat(bransje.inkludererVirksomhet(underenhetMedNæringskode("86101"))).isTrue()
        Assertions.assertThat(bransje.inkludererVirksomhet(underenhetMedNæringskode("00000"))).isFalse()
    }

    private fun underenhetMedNæringskode(næringskode: String): Underenhet.Næringsdrivende {
        return Underenhet.Næringsdrivende(
            etOrgnr(), Orgnr("053497180"), "Underenhet AS", enNæringskode(næringskode), 40
        )
    }
}
