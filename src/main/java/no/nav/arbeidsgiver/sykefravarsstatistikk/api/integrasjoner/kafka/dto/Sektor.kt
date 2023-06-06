package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto

enum class Sektor(val ssbSektorkode: Int) {
    STATLIG(1),
    KOMMUNAL(2),
    PRIVAT(3),
    FYLKESKOMMUNAL_FORVALTNING(9),
    UKJENT(0);

    companion object {
        fun fraSsbSektorkode(ssbSektorkode: Int): Sektor = values().find { it.ssbSektorkode == ssbSektorkode } ?: UKJENT
    }

}