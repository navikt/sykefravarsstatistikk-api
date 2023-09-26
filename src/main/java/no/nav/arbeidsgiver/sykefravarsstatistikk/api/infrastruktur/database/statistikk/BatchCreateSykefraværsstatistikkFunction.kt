package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.statistikk

fun interface BatchCreateSykefraværsstatistikkFunction {
    fun apply(): Int
}
