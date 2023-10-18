package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.domene.SykefraværMedKategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.domene.VirksomhetSykefravær

data class KafkaTopicValue(
    val virksomhetSykefravær: VirksomhetSykefravær,
    val næring5SifferSykefravær: List<SykefraværMedKategori>,
    val næringSykefravær: SykefraværMedKategori,
    val sektorSykefravær: SykefraværMedKategori,
    val landSykefravær: SykefraværMedKategori
)
