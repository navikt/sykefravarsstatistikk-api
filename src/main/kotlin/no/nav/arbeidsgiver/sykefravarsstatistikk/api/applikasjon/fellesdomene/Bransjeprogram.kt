package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

import ia.felles.definisjoner.bransjer.BransjeId
import ia.felles.definisjoner.bransjer.Bransje as Bransjer

object Bransjeprogram {
    val alleBransjer: List<Bransje> = Bransjer.entries.map { Bransje(it) }

    fun finnBransje(næringskode: Næringskode): Bransje? = alleBransjer.firstOrNull { bransje ->
            bransje.type.bransjeId.let {
                when (it) {
                    is BransjeId.Næring -> næringskode.næring.tosifferIdentifikator == it.næring
                    is BransjeId.Næringskoder -> it.næringskoder.contains(næringskode.femsifferIdentifikator)
                }
            }
        }
}
