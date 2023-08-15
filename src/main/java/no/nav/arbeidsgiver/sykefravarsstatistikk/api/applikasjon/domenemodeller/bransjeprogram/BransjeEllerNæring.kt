package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram

import io.vavr.control.Either
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næring
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næring.getNavn
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori

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
        return if (isBransje) verdi.left.getNavn() else verdi.get().navn
    }

    val næring: Næring
        get() = verdi.get()
}
