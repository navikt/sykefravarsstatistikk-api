package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

@Deprecated("Bruk Underenhet.Næringsdrivende", ReplaceWith("Underenhet.Næringsdrivende"))
data class UnderenhetLegacy(
    override var orgnr: Orgnr,
    var overordnetEnhetOrgnr: Orgnr? = null,
    override var navn: String,
    override var næringskode: BedreNæringskode,
    var antallAnsatte: Int? = null,
) : Virksomhet

