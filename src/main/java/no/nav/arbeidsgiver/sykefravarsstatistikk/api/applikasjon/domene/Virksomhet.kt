package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene

interface Virksomhet {
    val orgnr: Orgnr
    val navn: String?
    val næringskode: Næringskode5Siffer?
}