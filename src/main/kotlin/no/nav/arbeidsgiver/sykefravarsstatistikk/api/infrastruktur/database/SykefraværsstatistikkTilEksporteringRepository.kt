package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal.Companion.range
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
    @param:Qualifier("sykefravarsstatistikkJdbcTemplate") private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {
    fun hentSykefraværprosentLand(årstallOgKvartal: ÅrstallOgKvartal): SykefraværsstatistikkLand? {
        return try {
            val resultat = namedParameterJdbcTemplate.query(
                "select arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk "
                        + "from sykefravar_statistikk_land "
                        + "where arstall = :arstall and kvartal = :kvartal order by arstall, kvartal",
                MapSqlParameterSource()
                    .addValue("arstall", årstallOgKvartal.årstall)
                    .addValue("kvartal", årstallOgKvartal.kvartal)
            ) { rs: ResultSet, _: Int -> mapTilSykefraværsstatistikkLand(rs) }
            if (resultat.size != 1) {
                null
            } else resultat[0]
        } catch (e: EmptyResultDataAccessException) {
            null
        }
    }

    fun hentSykefraværprosentAlleSektorer(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<SykefraværsstatistikkSektor> {
        return try {
            namedParameterJdbcTemplate.query(
                "select sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk "
                        + "from sykefravar_statistikk_sektor "
                        + "where arstall = :arstall and kvartal = :kvartal "
                        + "order by arstall, kvartal, sektor_kode",
                MapSqlParameterSource()
                    .addValue("arstall", årstallOgKvartal.årstall)
                    .addValue("kvartal", årstallOgKvartal.kvartal)
            ) { rs: ResultSet, _: Int -> mapTilSykefraværsstatistikkSektor(rs) }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    fun hentSykefraværAlleSektorerFraOgMed(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<SykefraværsstatistikkSektor> {
        return try {
            namedParameterJdbcTemplate.query(
                "select sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk "
                        + "from sykefravar_statistikk_sektor "
                        + "where (arstall = :arstall and kvartal >= :kvartal) "
                        + "or (arstall > :arstall) "
                        + "order by arstall, kvartal, sektor_kode",
                MapSqlParameterSource()
                    .addValue("arstall", årstallOgKvartal.årstall)
                    .addValue("kvartal", årstallOgKvartal.kvartal)
            ) { rs: ResultSet, _: Int -> mapTilSykefraværsstatistikkSektor(rs) }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    /* Sykefraværsprosent Næring */
    fun hentSykefraværprosentAlleNæringer(
        sisteÅrstallOgKvartal: ÅrstallOgKvartal, antallKvartaler: Int
    ): List<SykefraværsstatistikkForNæring> {
        return if (antallKvartaler == 2) {
            hentSykefraværprosentAlleNæringer(sisteÅrstallOgKvartal)
        } else hentSykefraværprosentAlleNæringer(
            sisteÅrstallOgKvartal, sisteÅrstallOgKvartal.minusKvartaler(antallKvartaler - 1)
        )
    }

    fun hentSykefraværprosentAlleNæringer(
        årstallOgKvartal: ÅrstallOgKvartal?
    ): List<SykefraværsstatistikkForNæring> {
        return hentSykefraværprosentAlleNæringer(årstallOgKvartal, årstallOgKvartal)
    }

    fun hentSykefraværprosentAlleNæringer(
        fraÅrstallOgKvartal: ÅrstallOgKvartal?, tilÅrstallOgKvartal: ÅrstallOgKvartal?
    ): List<SykefraværsstatistikkForNæring> {
        return try {
            namedParameterJdbcTemplate.query(
                "select naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk "
                        + "from sykefravar_statistikk_naring "
                        + " where "
                        + getWhereClause(fraÅrstallOgKvartal, tilÅrstallOgKvartal)
                        + "order by (arstall, kvartal) desc, naring_kode"
            ) { rs: ResultSet, _: Int -> mapTilSykefraværsstatistikkNæring(rs) }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

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

    @JvmOverloads
    fun hentSykefraværAlleVirksomheter(
        fraÅrstallOgKvartal: ÅrstallOgKvartal?, tilÅrstallOgKvartal: ÅrstallOgKvartal? = fraÅrstallOgKvartal
    ): List<SykefraværsstatistikkVirksomhetUtenVarighet> {
        return try {
            namedParameterJdbcTemplate.query(
                "select arstall, kvartal, orgnr, "
                        + "sum(tapte_dagsverk) as tapte_dagsverk, "
                        + "sum(mulige_dagsverk) as mulige_dagsverk, "
                        + "sum(antall_personer) as antall_personer "
                        + "from sykefravar_statistikk_virksomhet "
                        + " where "
                        + getWhereClause(fraÅrstallOgKvartal, tilÅrstallOgKvartal)
                        + " group by arstall, kvartal, orgnr"
            ) { rs: ResultSet, _: Int -> mapTilSykefraværsstatistikkVirksomhet(rs) }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    fun hentSykefraværAlleVirksomheterGradert(
        fraÅrstallOgKvartal: ÅrstallOgKvartal,
        tilÅrstallOgKvartal: ÅrstallOgKvartal,
    ): List<SykefraværsstatistikkVirksomhetMedGradering> {
        return runCatching {
            // language=PostgreSQL
            namedParameterJdbcTemplate.query(
                """
                    select arstall, kvartal, orgnr, naring, naring_kode, rectype,
                    sum(antall_graderte_sykemeldinger) as antall_graderte_sykemeldinger,
                    sum(tapte_dagsverk_gradert_sykemelding) as tapte_dagsverk_gradert_sykemelding,
                    sum(antall_personer) as antall_personer,
                    sum(tapte_dagsverk) as tapte_dagsverk,
                    sum(mulige_dagsverk) as mulige_dagsverk
                    from sykefravar_statistikk_virksomhet_med_gradering
                    where ${getWhereClause(fraÅrstallOgKvartal, tilÅrstallOgKvartal)}
                    group by arstall, kvartal, orgnr
                """.trimIndent()
            ) { rs: ResultSet, _: Int -> mapTilSykefraværsstatistikkVirksomhetMedGradering(rs) }
        }.getOrElse {
            if (it is EmptyResultDataAccessException) {
                emptyList()
            } else {
                throw it
            }
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
        return hentSykefraværsstatistikkForBransjer(kvartaler, namedParameterJdbcTemplate)
    }

    @Throws(SQLException::class)
    private fun mapTilSykefraværsstatistikkLand(rs: ResultSet): SykefraværsstatistikkLand {
        return SykefraværsstatistikkLand(
            rs.getInt("arstall"),
            rs.getInt("kvartal"),
            rs.getInt("antall_personer"),
            rs.getBigDecimal("tapte_dagsverk"),
            rs.getBigDecimal("mulige_dagsverk")
        )
    }

    @Throws(SQLException::class)
    private fun mapTilSykefraværsstatistikkSektor(rs: ResultSet): SykefraværsstatistikkSektor {
        return SykefraværsstatistikkSektor(
            rs.getInt("arstall"),
            rs.getInt("kvartal"),
            rs.getString("sektor_kode"),
            rs.getInt("antall_personer"),
            rs.getBigDecimal("tapte_dagsverk"),
            rs.getBigDecimal("mulige_dagsverk")
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

    @Throws(SQLException::class)
    private fun mapTilSykefraværsstatistikkVirksomhet(
        rs: ResultSet
    ): SykefraværsstatistikkVirksomhetUtenVarighet {
        return SykefraværsstatistikkVirksomhetUtenVarighet(
            rs.getInt("arstall"),
            rs.getInt("kvartal"),
            rs.getString("orgnr"),
            rs.getInt("antall_personer"),
            rs.getBigDecimal("tapte_dagsverk"),
            rs.getBigDecimal("mulige_dagsverk")


        )
    }

    @Throws(SQLException::class)
    private fun mapTilSykefraværsstatistikkVirksomhetMedGradering(
        resultSet: ResultSet
    ): SykefraværsstatistikkVirksomhetMedGradering = SykefraværsstatistikkVirksomhetMedGradering(
        årstall = resultSet.getInt("arstall"),
        kvartal = resultSet.getInt("kvartal"),
        orgnr = resultSet.getString("orgnr"),
        næring = resultSet.getString("naring"),
        næringkode = resultSet.getString("naring_kode"),
        rectype = resultSet.getString("rectype"),
        antallGraderteSykemeldinger = resultSet.getInt("antall_graderte_sykemeldinger"),
        tapteDagsverkGradertSykemelding = resultSet.getBigDecimal("tapte_dagsverk_gradert_sykemelding"),
        antallSykemeldinger = resultSet.getInt("antall_sykemeldinger"),
        antallPersoner = resultSet.getInt("antall_personer"),
        tapteDagsverk = resultSet.getBigDecimal("tapte_dagsverk"),
        muligeDagsverk = resultSet.getBigDecimal("mulige_dagsverk"),
    )

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
