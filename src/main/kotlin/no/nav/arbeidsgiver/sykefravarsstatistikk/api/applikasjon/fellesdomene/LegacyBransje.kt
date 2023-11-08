package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

import ia.felles.definisjoner.bransjer.BransjeId
import ia.felles.definisjoner.bransjer.Bransje as Bransjer

data class LegacyBransje(
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
        return type.bransjeId.let {
            when (it) {
                is BransjeId.Næring -> underenhet.næringskode.næring.tosifferIdentifikator == it.næring
                is BransjeId.Næringskoder -> it.næringskoder.contains(underenhet.næringskode.femsifferIdentifikator)
            }
        }
    }

}