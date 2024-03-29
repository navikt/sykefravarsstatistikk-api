package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

data class OverordnetEnhet(
    override val orgnr: Orgnr,
    override val navn: String,
    override val næringskode: Næringskode,
    val sektor: Sektor?,
    val antallAnsatte: Int? = 0
) : Virksomhet