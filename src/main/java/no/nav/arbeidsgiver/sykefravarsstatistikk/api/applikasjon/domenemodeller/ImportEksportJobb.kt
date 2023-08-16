package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

enum class ImportEksportJobb(val rekkefølge: Int) {
    IMPORTERT_STATISTIKK(0), IMPORTERT_VIRKSOMHETDATA(1), FORBEREDT_NESTE_EKSPORT(2), EKSPORTERT_PÅ_KAFKA(3);
}