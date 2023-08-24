package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

data class OverordnetEnhet(
    override val orgnr: Orgnr,
    override val navn: String,
    override val næringskode: Næringskode5Siffer,
    val sektor: Sektor?,
    val antallAnsatte: Int? = 0
) : Virksomhet