package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Underenhet
import ia.felles.definisjoner.bransjer.Bransje as FellesBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Næring as DomeneNæring

sealed class Aggregeringskategorier {
    data object Land : Aggregeringskategorier()
    data class Næring(
        val næring: DomeneNæring
    ) : Aggregeringskategorier()

    data class Bransje(
        val bransje: FellesBransje
    ) : Aggregeringskategorier()

    data class Virksomhet(
        val virksomhet: Underenhet.Næringsdrivende
    ) : Aggregeringskategorier()
}