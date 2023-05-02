package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles

interface Virksomhet {
    val orgnr: Orgnr
    val navn: String?
    val næringskode: Næringskode5Siffer?
}