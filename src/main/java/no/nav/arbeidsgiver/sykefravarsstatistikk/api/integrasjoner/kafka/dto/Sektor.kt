package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto

enum class Sektor(val ssbSektorkode: Int) {
    STATLIG(1),
    KOMMUNAL(2),
    PRIVAT(3),
    UKJENT(4),
}