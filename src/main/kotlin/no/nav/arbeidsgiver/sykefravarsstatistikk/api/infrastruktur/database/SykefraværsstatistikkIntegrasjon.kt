package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

abstract class SykefraværsstatistikkIntegrasjon(@JvmField val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {
    companion object {
        const val ARSTALL = "arstall"
        const val KVARTAL = "kvartal"
    }
}
