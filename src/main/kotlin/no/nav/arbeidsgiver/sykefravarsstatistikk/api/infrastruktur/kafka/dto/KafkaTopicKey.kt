package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto

data class KafkaTopicKey(val orgnr: String, val kvartal: Int, val årstall: Int)