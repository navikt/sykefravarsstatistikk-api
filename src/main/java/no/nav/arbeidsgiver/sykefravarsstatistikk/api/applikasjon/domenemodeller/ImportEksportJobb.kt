package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

enum class ImportEksportJobb( val rekkefølge: Int) {
    IMPORTERT_STATISTIKK(0),
    IMPORTERT_VIRKSOMHETDATA(1),
    IMPORTERT_NÆRINGSKODEMAPPING(2),
    FORBEREDT_NESTE_EKSPORT_LEGACY(3),
    EKSPORTERT_LEGACY(4),
    EKSPORTERT_METADATA_VIRKSOMHET(5),
    EKSPORTERT_PER_STATISTIKKATEGORI(6)
}