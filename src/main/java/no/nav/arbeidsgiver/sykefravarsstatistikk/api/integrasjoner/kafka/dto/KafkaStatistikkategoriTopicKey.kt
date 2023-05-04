package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori

data class KafkaStatistikkategoriTopicKey(
    val kategori: Statistikkategori,
    val kode: String,
    val kvartal: Int,
    val årstall: Int,
)