package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

sealed class Underenhet {
    abstract val orgnr: Orgnr
    abstract val overordnetEnhetOrgnr: Orgnr
    abstract val navn: String
    abstract val antallAnsatte: Int

    data class Næringsdrivende(
        override val orgnr: Orgnr,
        override val overordnetEnhetOrgnr: Orgnr,
        override val navn: String,
        override val næringskode: BedreNæringskode,
        override val antallAnsatte: Int
    ) : Underenhet(), Virksomhet

    data class IkkeNæringsdrivende(
        override val orgnr: Orgnr,
        override val overordnetEnhetOrgnr: Orgnr,
        override val navn: String,
        override val antallAnsatte: Int
    ) : Underenhet()
}