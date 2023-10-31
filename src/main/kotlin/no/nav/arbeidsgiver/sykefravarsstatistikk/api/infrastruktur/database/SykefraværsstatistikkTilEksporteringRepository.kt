package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal.Companion.range
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import java.util.stream.Collectors

@Component
class SykefraværsstatistikkTilEksporteringRepository(
    @param:Qualifier("sykefravarsstatistikkJdbcTemplate") private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    private val sykefraværStatistikkNæringRepository: SykefraværStatistikkNæringRepository,
) {

    fun hentSykefraværprosentForAlleNæringskoder(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<SykefraværsstatistikkForNæringskode> {
        return hentSykefraværprosentForAlleNæringskoder(årstallOgKvartal, årstallOgKvartal)
    }

    fun hentSykefraværprosentForAlleNæringskoder(
        fraÅrstallOgKvartal: ÅrstallOgKvartal, tilÅrstallOgKvartal: ÅrstallOgKvartal?
    ): List<SykefraværsstatistikkForNæringskode> {
        return try {
            namedParameterJdbcTemplate.query(
                "select naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk "
                        + "from sykefravar_statistikk_naring5siffer "
                        + " where "
                        + getWhereClause(fraÅrstallOgKvartal, tilÅrstallOgKvartal)
                        + "order by (arstall, kvartal) desc, naring_kode",
                MapSqlParameterSource()
                    .addValue("arstall", fraÅrstallOgKvartal.årstall)
                    .addValue("kvartal", fraÅrstallOgKvartal.kvartal)
            ) { rs: ResultSet, _: Int -> mapTilSykefraværsstatistikkNæring5Siffer(rs) }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    fun hentSykefraværAlleNæringerFraOgMed(
        fraÅrstallOgKvartal: ÅrstallOgKvartal
    ): List<SykefraværsstatistikkForNæring> {
        return try {
            namedParameterJdbcTemplate.query(
                "select arstall, kvartal, naring_kode, antall_personer, tapte_dagsverk, mulige_dagsverk "
                        + "from sykefravar_statistikk_naring "
                        + "where (arstall = :arstall and kvartal >= :kvartal) "
                        + "or (arstall > :arstall) "
                        + "group by arstall, kvartal, id order by arstall, kvartal, naring_kode",
                MapSqlParameterSource()
                    .addValue("arstall", fraÅrstallOgKvartal.årstall)
                    .addValue("kvartal", fraÅrstallOgKvartal.kvartal)
            ) { rs: ResultSet, _: Int -> mapTilSykefraværsstatistikkNæring(rs) }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    fun hentSykefraværAlleBransjer(kvartaler: List<ÅrstallOgKvartal>): List<SykefraværsstatistikkBransje> {
        return hentSykefraværsstatistikkForBransjer(
            kvartaler,
            namedParameterJdbcTemplate,
            sykefraværStatistikkNæringRepository
        )
    }

    @Throws(SQLException::class)
    private fun mapTilSykefraværsstatistikkNæring5Siffer(rs: ResultSet): SykefraværsstatistikkForNæringskode {
        return SykefraværsstatistikkForNæringskode(
            rs.getInt("arstall"),
            rs.getInt("kvartal"),
            rs.getString("naring_kode"),
            rs.getInt("antall_personer"),
            rs.getBigDecimal("tapte_dagsverk"),
            rs.getBigDecimal("mulige_dagsverk")
        )
    }

    companion object {
        // Utilities
        private fun getWhereClause(
            fraÅrstallOgKvartal: ÅrstallOgKvartal?, tilÅrstallOgKvartal: ÅrstallOgKvartal?
        ): String {

            // Alltid sjekk input før verdi skal til SQL
            sjekkÅrstallOgKvartal(fraÅrstallOgKvartal!!, tilÅrstallOgKvartal!!)
            val årstallOgKvartalListe = range(fraÅrstallOgKvartal, tilÅrstallOgKvartal)
            return årstallOgKvartalListe.stream()
                .map { (årstall, kvartal): ÅrstallOgKvartal ->
                    String.format(
                        "(arstall = %d and kvartal = %d) ",
                        årstall, kvartal
                    )
                }
                .collect(Collectors.joining("or "))
        }

        private fun sjekkÅrstallOgKvartal(vararg årstallOgKvartalListe: ÅrstallOgKvartal) {
            Arrays.stream(årstallOgKvartalListe)
                .forEach { (årstall, kvartal): ÅrstallOgKvartal ->
                    require(!(årstall < 2010 || årstall > 2100)) {
                        String.format(
                            "Årstall skal være mellom 2010 og 2100. Fikk '%d'",
                            årstall
                        )
                    }
                    require(!(kvartal < 1 || kvartal > 4)) {
                        String.format(
                            "Kvartal skal være mellom 1 og 4. Fikk '%d'",
                            kvartal
                        )
                    }
                }
        }

        @Throws(SQLException::class)
        fun mapTilSykefraværsstatistikkNæring(rs: ResultSet): SykefraværsstatistikkForNæring {
            return SykefraværsstatistikkForNæring(
                rs.getInt("arstall"),
                rs.getInt("kvartal"),
                rs.getString("naring_kode"),
                rs.getInt("antall_personer"),
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk")
            )
        }
    }
}
