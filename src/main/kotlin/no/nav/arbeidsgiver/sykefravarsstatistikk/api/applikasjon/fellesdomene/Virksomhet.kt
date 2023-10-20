package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene

interface Virksomhet {
    val orgnr: Orgnr
    val navn: String
    val næringskode: Næringskode
}