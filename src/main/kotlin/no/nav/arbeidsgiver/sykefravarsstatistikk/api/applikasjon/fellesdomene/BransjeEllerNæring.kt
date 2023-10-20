package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

import io.vavr.control.Either

class BransjeEllerNæring {
    val verdi: Either<Bransje, Næring>

    constructor(bransje: Bransje) {
        verdi = Either.left(bransje)
    }

    constructor(næring: Næring) {
        verdi = Either.right(næring)
    }

    val statistikkategori: Statistikkategori
        get() = if (verdi.isLeft) {
            Statistikkategori.BRANSJE
        } else {
            Statistikkategori.NÆRING
        }
    val isBransje: Boolean
        get() = verdi.isLeft

    fun getBransje(): Bransje {
        return verdi.left
    }

    fun navn(): String {
        return if (isBransje) verdi.left.navn else verdi.get().navn
    }

    val næring: Næring
        get() = verdi.get()
}
