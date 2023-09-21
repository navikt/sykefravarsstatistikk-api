package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram

import ia.felles.definisjoner.bransjer.Bransjer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Virksomhet

data class Bransje(
    val type: Bransjer,
) {

    val navn = type.navn
    val identifikatorer = type.næringskoder

    init {
        require(erDefinertPåTosiffernivå() || erDefinertPåFemsiffernivå())
        { "Støtter kun bransjer som er spesifisert av enten 2 eller 5 sifre" }
    }

    fun erDefinertPåTosiffernivå() = identifikatorer.all { it.length == 2 }

    fun erDefinertPåFemsiffernivå() = identifikatorer.all { it.length == 5 }

    fun inkludererVirksomhet(underenhet: Virksomhet): Boolean {
        return inkludererNæringskode(underenhet.næringskode)
    }

    private fun inkludererNæringskode(næringskode: Næringskode): Boolean {
        return identifikatorer.stream().anyMatch { prefix: String? ->
            næringskode.femsifferIdentifikator.startsWith(prefix!!)
        }
    }

    fun inkludererNæringskode(næringskode5Siffer: String?): Boolean {
        return if (næringskode5Siffer == null) {
            false
        } else identifikatorer.stream().anyMatch { prefix: String? ->
            næringskode5Siffer.startsWith(
                prefix!!
            )
        }
    }
}
