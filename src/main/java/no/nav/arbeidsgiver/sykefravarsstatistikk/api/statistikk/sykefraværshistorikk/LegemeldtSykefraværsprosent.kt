package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori
import java.math.BigDecimal

data class LegemeldtSykefraværsprosent(
    val type: Statistikkategori? = null,
    val label: String? = null,
    val prosent: BigDecimal? = null
)