package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SykefraværMedKategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.VirksomhetSykefravær

data class KafkaTopicValue(
    val virksomhetSykefravær: VirksomhetSykefravær,
    val næring5SifferSykefravær: List<SykefraværMedKategori>,
    val næringSykefravær: SykefraværMedKategori,
    val sektorSykefravær: SykefraværMedKategori,
    val landSykefravær: SykefraværMedKategori
)
