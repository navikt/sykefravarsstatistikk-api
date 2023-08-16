package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import com.fasterxml.jackson.annotation.JsonProperty

data class KvartalsvisSykefraværshistorikk(
    val type: Statistikkategori? = null,
    val label: String? = null,
    @JsonProperty("kvartalsvisSykefraværsprosent")
    val sykefraværForEttKvartal: List<SykefraværForEttKvartal>? = null
)
