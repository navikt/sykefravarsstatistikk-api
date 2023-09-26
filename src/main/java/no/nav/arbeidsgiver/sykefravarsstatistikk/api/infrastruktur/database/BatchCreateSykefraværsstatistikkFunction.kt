package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

fun interface BatchCreateSykefraværsstatistikkFunction {
    fun apply(): Int
}
