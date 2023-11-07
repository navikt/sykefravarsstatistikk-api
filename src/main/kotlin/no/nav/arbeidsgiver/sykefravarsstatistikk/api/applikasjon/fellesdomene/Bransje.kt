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

    fun inkludererVirksomhet(underenhet: Virksomhet): Boolean {
        return inkludererNæringskode(underenhet.næringskode)
    }

    fun inkludererNæringskode(næringskode: Næringskode): Boolean {
        return type.bransjeId.let {
            when (it) {
                is BransjeId.Næring -> næringskode.næring.tosifferIdentifikator == it.næring
                is BransjeId.Næringskoder -> it.næringskoder.contains(næringskode.femsifferIdentifikator)
            }
        }
    }
}
