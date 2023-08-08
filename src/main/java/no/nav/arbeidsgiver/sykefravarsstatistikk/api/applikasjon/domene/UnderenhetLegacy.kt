package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene

@Deprecated("Bruk Underenhet.Næringsdrivende", ReplaceWith("Underenhet.Næringsdrivende"))
data class UnderenhetLegacy(
    override var orgnr: Orgnr,
    var overordnetEnhetOrgnr: Orgnr? = null,
    override var navn: String? = null,
    override var næringskode: Næringskode5Siffer? = null,
    var antallAnsatte: Int? = null,
) : Virksomhet

