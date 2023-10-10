package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

interface Virksomhet {
    val orgnr: Orgnr
    val navn: String
    val næringskode: Næringskode
}