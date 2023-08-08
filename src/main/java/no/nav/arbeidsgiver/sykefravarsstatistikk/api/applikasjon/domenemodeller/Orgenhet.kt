package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

data class Orgenhet(
    val orgnr: Orgnr,
    val navn: String?,
    val rectype: String?,
    val sektor: String?,
    val næring: String?,
    val næringskode: String?,
    val årstallOgKvartal: ÅrstallOgKvartal
)