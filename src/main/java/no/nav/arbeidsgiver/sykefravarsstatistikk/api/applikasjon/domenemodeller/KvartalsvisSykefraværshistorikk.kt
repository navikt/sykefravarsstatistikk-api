package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Data

@Data
class KvartalsvisSykefraværshistorikk {
    private val type: Statistikkategori? = null
    private val label: String? = null

    @JsonProperty("kvartalsvisSykefraværsprosent")
    private val sykefraværForEttKvartal: List<SykefraværForEttKvartal>? = null
}
