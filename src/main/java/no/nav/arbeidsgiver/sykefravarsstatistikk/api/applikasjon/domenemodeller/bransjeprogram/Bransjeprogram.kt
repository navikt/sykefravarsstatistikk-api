package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram

import ia.felles.definisjoner.bransjer.Bransjer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Virksomhet
import java.util.*

object Bransjeprogram {
    val alleBransjer: List<Bransje> = Bransjer.entries.map { Bransje(it) }

    @JvmStatic
    fun finnBransje(underenhet: Virksomhet?): Optional<Bransje> = finnBransje(underenhet?.næringskode)

    @JvmStatic
    fun finnBransje(næringskode: Næringskode?): Optional<Bransje> =
        Optional.ofNullable(alleBransjer.firstOrNull
        {
            it.inkludererNæringskode(næringskode?.femsifferIdentifikator)
        })
}
