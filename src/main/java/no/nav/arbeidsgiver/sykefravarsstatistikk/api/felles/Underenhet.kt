package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles

data class Underenhet(
    override var orgnr: Orgnr,
    var overordnetEnhetOrgnr: Orgnr? = null,
    override var navn: String? = null,
    override var næringskode: Næringskode5Siffer? = null,
    var antallAnsatte: Int? = null,
) : Virksomhet