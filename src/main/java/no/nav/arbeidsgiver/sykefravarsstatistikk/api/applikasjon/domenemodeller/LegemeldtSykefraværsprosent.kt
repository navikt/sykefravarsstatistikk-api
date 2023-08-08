package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import java.math.BigDecimal

data class LegemeldtSykefraværsprosent(
    val type: Statistikkategori? = null,
    val label: String? = null,
    val prosent: BigDecimal? = null
)