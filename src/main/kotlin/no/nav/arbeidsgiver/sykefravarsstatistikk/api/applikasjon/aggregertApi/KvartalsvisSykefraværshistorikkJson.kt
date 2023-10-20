package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertApi

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværForEttKvartal

data class KvartalsvisSykefraværshistorikkJson(
    val type: Statistikkategori? = null,
    val label: String? = null,
    @JsonProperty("kvartalsvisSykefraværsprosent")
    val sykefraværForEttKvartal: List<SykefraværForEttKvartal>? = null
)
