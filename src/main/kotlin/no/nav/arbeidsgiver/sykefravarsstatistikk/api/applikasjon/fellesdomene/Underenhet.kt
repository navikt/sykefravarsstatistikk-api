package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

sealed class Underenhet {
    abstract val orgnr: Orgnr

    data class Næringsdrivende(
        override val orgnr: Orgnr,
        val overordnetEnhetOrgnr: Orgnr,
        override val navn: String,
        override val næringskode: Næringskode,
        val antallAnsatte: Int
    ) : Underenhet(), Virksomhet

    data class IkkeNæringsdrivende(override val orgnr: Orgnr) : Underenhet()
}