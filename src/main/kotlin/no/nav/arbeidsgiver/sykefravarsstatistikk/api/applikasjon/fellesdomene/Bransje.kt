package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

import ia.felles.definisjoner.bransjer.BransjeId
import ia.felles.definisjoner.bransjer.Bransje as Bransjer

data class Bransje(
    val type: Bransjer,
) {
    val navn = type.navn
    val identifikatorer = type.bransjeId.let {
        when (it) {
            is BransjeId.Næring -> listOf(it.næring)
            is BransjeId.Næringskoder -> it.næringskoder
        }
    }

    init {
        require(erDefinertPåTosiffernivå() || erDefinertPåFemsiffernivå())
        { "Støtter kun bransjer som er spesifisert av enten 2 eller 5 sifre" }
    }

    fun erDefinertPåTosiffernivå() = type.bransjeId is BransjeId.Næring

    fun erDefinertPåFemsiffernivå() = type.bransjeId is BransjeId.Næringskoder

    fun inkludererVirksomhet(underenhet: Virksomhet): Boolean {
        return inkludererNæringskode(underenhet.næringskode)
    }

    fun inkludererNæringskode(næringskode: Næringskode?): Boolean {
        if (næringskode == null) {
            return false
        }
        return type.bransjeId.let {
            when (it) {
                is BransjeId.Næring -> næringskode.næring.tosifferIdentifikator == it.næring
                is BransjeId.Næringskoder -> it.næringskoder.contains(næringskode.femsifferIdentifikator)
            }
        }
    }
}
