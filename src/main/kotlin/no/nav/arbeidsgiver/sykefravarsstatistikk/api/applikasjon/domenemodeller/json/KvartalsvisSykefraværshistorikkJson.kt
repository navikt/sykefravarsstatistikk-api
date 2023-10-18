package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.json

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.eksportering.SykefraværForEttKvartal

data class KvartalsvisSykefraværshistorikkJson(
    val type: Statistikkategori? = null,
    val label: String? = null,
    @JsonProperty("kvartalsvisSykefraværsprosent")
    val sykefraværForEttKvartal: List<SykefraværForEttKvartal>? = null
)
