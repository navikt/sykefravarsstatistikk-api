package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.sql.SQLException

@Component
class KvartalsvisSykefraværRepository(
    @param:Qualifier("sykefravarsstatistikkJdbcTemplate") private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
) {
    fun hentKvartalsvisSykefraværprosentBransje(bransje: Bransje): List<SykefraværForEttKvartal> {
        // TODO how can this even work
        return try {
            namedParameterJdbcTemplate.query(
                "SELECT sum(tapte_dagsverk) as tapte_dagsverk, sum(mulige_dagsverk) as mulige_dagsverk, sum(antall_personer) as antall_personer, arstall, kvartal "
                        + "FROM sykefravar_statistikk_naring5siffer "
                        + "where naring_kode in (:naringKoder) "
                        + "group by arstall, kvartal "
                        + "ORDER BY arstall, kvartal ",
                MapSqlParameterSource()
                        .addValue("naringKoder", bransje.identifikatorer)
            ) { rs: ResultSet, _: Int -> mapTilKvartalsvisSykefraværprosent(rs) }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    @Throws(SQLException::class)
    private fun mapTilKvartalsvisSykefraværprosent(rs: ResultSet): SykefraværForEttKvartal {
        return SykefraværForEttKvartal(
            ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
            rs.getBigDecimal("tapte_dagsverk"),
            rs.getBigDecimal("mulige_dagsverk"),
            rs.getInt("antall_personer")
        )
    }
}
