package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

enum class ImportEksportJobb {
    IMPORTERT_STATISTIKK,
    IMPORTERT_VIRKSOMHETDATA,
    EKSPORTERT_METADATA_VIRKSOMHET,
    EKSPORTERT_PER_STATISTIKKATEGORI
}