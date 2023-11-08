package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

import ia.felles.definisjoner.bransjer.Bransje
import ia.felles.definisjoner.bransjer.BransjeId
import ia.felles.definisjoner.bransjer.Bransje as Bransjer

object Bransjeprogram {
    val alleBransjer: List<LegacyBransje> = Bransjer.entries.map { LegacyBransje(it) }

    fun finnBransje(næringskode: Næringskode): Bransje? = Bransjer.entries.firstOrNull { bransje ->
            bransje.bransjeId.let {
                when (it) {
                    is BransjeId.Næring -> næringskode.næring.tosifferIdentifikator == it.næring
                    is BransjeId.Næringskoder -> it.næringskoder.contains(næringskode.femsifferIdentifikator)
                }
            }
        }
}
