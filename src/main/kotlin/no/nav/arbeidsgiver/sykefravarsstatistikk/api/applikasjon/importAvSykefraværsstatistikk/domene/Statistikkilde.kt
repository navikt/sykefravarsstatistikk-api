package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene

enum class Statistikkilde(val tabell: String) {
    SEKTOR("sykefravar_statistikk_sektor"),
    NÆRING("sykefravar_statistikk_naring"),
    NÆRING_5_SIFFER("sykefravar_statistikk_naring5siffer"),
}
