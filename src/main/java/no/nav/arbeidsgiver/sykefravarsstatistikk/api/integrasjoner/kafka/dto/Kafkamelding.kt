package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto

interface Kafkamelding {
    val nøkkel: String
    val innhold: String
}