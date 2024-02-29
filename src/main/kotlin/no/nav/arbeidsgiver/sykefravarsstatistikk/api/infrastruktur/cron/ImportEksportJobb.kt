package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

enum class ImportEksportJobb(val måKjøreEtter: ImportEksportJobb?) {
    IMPORTERT_STATISTIKK(måKjøreEtter = null),
    IMPORTERT_VIRKSOMHETDATA(måKjøreEtter = IMPORTERT_STATISTIKK),
    EKSPORTERT_METADATA_VIRKSOMHET(måKjøreEtter = IMPORTERT_VIRKSOMHETDATA),
    EKSPORTERT_PER_STATISTIKKATEGORI(måKjøreEtter = EKSPORTERT_METADATA_VIRKSOMHET)
}

fun List<ImportEksportJobb>.oppfyllerKraveneTilÅStarte(denneJobben: ImportEksportJobb) =
    this.manglerJobben(denneJobben) && påkrevdJobbErKjørtFor(denneJobben)

private fun List<ImportEksportJobb>.manglerJobben(jobb: ImportEksportJobb) = this.none { it == jobb }

private fun List<ImportEksportJobb>.påkrevdJobbErKjørtFor(denneJobben: ImportEksportJobb) =
    this.contains(denneJobben.måKjøreEtter)
