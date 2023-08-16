package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næringskode5Siffer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.UnderenhetLegacy

data class Bransje(
    val type: ArbeidsmiljøportalenBransje, val navn: String, val koderSomSpesifisererNæringer: List<String>
) {
    init {
        require(erDefinertPåTosiffernivå() || erDefinertPåFemsiffernivå())
        { "Støtter kun bransjer som er spesifisert av enten 2 eller 5 sifre" }
    }

    fun erDefinertPåTosiffernivå() = koderSomSpesifisererNæringer.all { it.length == 2 }

    fun erDefinertPåFemsiffernivå() = koderSomSpesifisererNæringer.all { it.length == 5 }

    fun inkludererVirksomhet(underenhet: UnderenhetLegacy): Boolean {
        return inkludererNæringskode(underenhet.næringskode)
    }

    private fun inkludererNæringskode(næringskode5Siffer: Næringskode5Siffer?): Boolean {
        val næringskode = næringskode5Siffer!!.kode
        return koderSomSpesifisererNæringer.stream().anyMatch { prefix: String? ->
            næringskode.startsWith(
                prefix!!
            )
        }
    }

    fun inkludererNæringskode(næringskode5Siffer: String?): Boolean {
        return if (næringskode5Siffer == null) {
            false
        } else koderSomSpesifisererNæringer.stream().anyMatch { prefix: String? ->
            næringskode5Siffer.startsWith(
                prefix!!
            )
        }
    }
}
