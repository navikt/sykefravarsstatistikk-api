package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

import ia.felles.definisjoner.bransjer.Bransje

class BransjeEllerNæring {
    val bransje: Bransje?
    val næring: Næring?

    constructor(bransje: Bransje) {
        this.bransje = bransje
        this.næring = null
    }

    constructor(næring: Næring) {
        this.bransje = null
        this.næring = næring
    }

    val statistikkategori: Statistikkategori
        get() = if (bransje != null) {
            Statistikkategori.BRANSJE
        } else {
            Statistikkategori.NÆRING
        }

    fun navn(): String = bransje?.navn ?: næring!!.navn
}
