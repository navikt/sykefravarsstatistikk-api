package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto

interface Kafkamelding {
    val nøkkel: String
    val innhold: String
}