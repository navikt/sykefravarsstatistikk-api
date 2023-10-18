package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværForEttKvartal
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
    @param:Qualifier("sykefravarsstatistikkJdbcTemplate") private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {
    fun hentKvartalsvisSykefraværprosentLand(): List<SykefraværForEttKvartal> {
        return try {
            namedParameterJdbcTemplate.query(
                "SELECT tapte_dagsverk, mulige_dagsverk, antall_personer, arstall, kvartal "
                        + "FROM sykefravar_statistikk_land "
                        + "ORDER BY arstall, kvartal ",
                HashMap<String, Any?>()
            ) { rs: ResultSet, _: Int -> mapTilKvartalsvisSykefraværprosent(rs) }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    fun hentKvartalsvisSykefraværprosentSektor(sektor: Sektor): List<SykefraværForEttKvartal> {
        return try {
            namedParameterJdbcTemplate.query(
                "SELECT tapte_dagsverk, mulige_dagsverk, antall_personer, arstall, kvartal "
                        + "FROM sykefravar_statistikk_sektor "
                        + "where sektor_kode = :sektorKode "
                        + "ORDER BY arstall, kvartal ",
                MapSqlParameterSource().addValue("sektorKode", sektor.sektorkode)
            ) { rs: ResultSet, _: Int -> mapTilKvartalsvisSykefraværprosent(rs) }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    fun hentKvartalsvisSykefraværprosentNæring(næring: Næring): List<SykefraværForEttKvartal> {
        return try {
            namedParameterJdbcTemplate.query(
                "SELECT tapte_dagsverk, mulige_dagsverk, antall_personer, arstall, kvartal "
                        + "FROM sykefravar_statistikk_naring "
                        + "where naring_kode = :naring "
                        + "ORDER BY arstall, kvartal ",
                MapSqlParameterSource().addValue("naring", næring.tosifferIdentifikator)
            ) { rs: ResultSet, _: Int -> mapTilKvartalsvisSykefraværprosent(rs) }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    fun hentKvartalsvisSykefraværprosentBransje(bransje: Bransje): List<SykefraværForEttKvartal> {
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

    fun hentKvartalsvisSykefraværprosentVirksomhet(
        virksomhet: Virksomhet
    ): List<SykefraværForEttKvartal> {
        return try {
            namedParameterJdbcTemplate.query(
                "SELECT sum(tapte_dagsverk) as tapte_dagsverk,"
                        + "sum(mulige_dagsverk) as mulige_dagsverk,"
                        + "sum(antall_personer) as antall_personer,"
                        + "arstall, kvartal "
                        + "FROM sykefravar_statistikk_virksomhet "
                        + "where orgnr = :orgnr "
                        + "GROUP BY arstall, kvartal "
                        + "ORDER BY arstall, kvartal ",
                MapSqlParameterSource().addValue("orgnr", virksomhet.orgnr.verdi)
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
