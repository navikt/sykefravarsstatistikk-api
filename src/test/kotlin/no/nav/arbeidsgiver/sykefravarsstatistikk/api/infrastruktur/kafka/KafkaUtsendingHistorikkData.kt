package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka

import java.time.LocalDateTime

class KafkaUtsendingHistorikkData(
  @JvmField var orgnr: String, @JvmField var key: String, @JvmField var value: String, @JvmField var opprettet: LocalDateTime
)
