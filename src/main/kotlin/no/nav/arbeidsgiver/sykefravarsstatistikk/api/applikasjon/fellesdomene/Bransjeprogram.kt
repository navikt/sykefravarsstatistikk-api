package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

import java.util.*
import ia.felles.definisjoner.bransjer.Bransje as Bransjer

object Bransjeprogram {
    val alleBransjer: List<Bransje> = Bransjer.entries.map { Bransje(it) }


    fun finnBransje(underenhet: Virksomhet): Optional<Bransje> = finnBransje(underenhet.næringskode)


    fun finnBransje(næringskode: Næringskode): Optional<Bransje> =
        Optional.ofNullable(
            alleBransjer.firstOrNull
        {
            it.inkludererNæringskode(næringskode)
        })
}
