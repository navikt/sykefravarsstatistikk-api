package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

enum class Klassifikasjonskilde(@JvmField val tabell: String) {
    SEKTOR("sektor"),
    NÆRING("naring")
}
