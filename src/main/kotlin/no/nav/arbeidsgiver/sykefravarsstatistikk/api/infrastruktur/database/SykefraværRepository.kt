package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertApi.domene.Sykefraværsdata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Bransjeprogram.finnBransje
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

@Component
class SykefraværRepository(
    @param:Qualifier("sykefravarsstatistikkJdbcTemplate") private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {
    fun hentUmaskertSykefravær(
        virksomhet: Virksomhet, fraÅrstallOgKvartal: ÅrstallOgKvartal
    ): List<UmaskertSykefraværForEttKvartal> {
        return try {
            namedParameterJdbcTemplate.query(
                "SELECT sum(tapte_dagsverk) as tapte_dagsverk,"
                        + "sum(mulige_dagsverk) as mulige_dagsverk,"
                        + "sum(antall_personer) as antall_personer,"
                        + "arstall, kvartal "
                        + "FROM sykefravar_statistikk_virksomhet "
                        + "where orgnr = :orgnr "
                        + "and ("
                        + "  (arstall = :arstall and kvartal >= :kvartal) "
                        + "  or "
                        + "  (arstall > :arstall)"
                        + ") "
                        + "GROUP BY arstall, kvartal "
                        + "ORDER BY arstall, kvartal ",
                MapSqlParameterSource()
                    .addValue("orgnr", virksomhet.orgnr.verdi)
                    .addValue("arstall", fraÅrstallOgKvartal.årstall)
                    .addValue("kvartal", fraÅrstallOgKvartal.kvartal)
            ) { rs: ResultSet, _: Int -> mapTilUmaskertSykefraværForEttKvartal(rs) }.sorted()
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    fun hentUmaskertSykefravær(
        bransje: Bransje, fraÅrstallOgKvartal: ÅrstallOgKvartal
    ): List<UmaskertSykefraværForEttKvartal> {
        require(!bransje.erDefinertPåTosiffernivå()) { "Denne metoden funker bare for 5-siffer næringskoder" }
        return try {
            namedParameterJdbcTemplate.query(
                "SELECT sum(tapte_dagsverk) as tapte_dagsverk, sum(mulige_dagsverk) as "
                        + "mulige_dagsverk, sum(antall_personer) as antall_personer, arstall, "
                        + "kvartal "
                        + "FROM sykefravar_statistikk_naring5siffer "
                        + "where naring_kode in (:naringKoder) "
                        + "and ("
                        + "  (arstall = :arstall and kvartal >= :kvartal) "
                        + "  or "
                        + "  (arstall > :arstall)"
                        + ") "
                        + "group by arstall, kvartal "
                        + "ORDER BY arstall, kvartal ",
                MapSqlParameterSource()
                    .addValue("naringKoder", bransje.identifikatorer)
                    .addValue("arstall", fraÅrstallOgKvartal.årstall)
                    .addValue("kvartal", fraÅrstallOgKvartal.kvartal)
            ) { rs: ResultSet, _: Int -> mapTilUmaskertSykefraværForEttKvartal(rs) }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    fun hentUmaskertSykefravær(
        næring: Næring, fraÅrstallOgKvartal: ÅrstallOgKvartal
    ): List<UmaskertSykefraværForEttKvartal> {
        return try {
            namedParameterJdbcTemplate.query(
                "SELECT tapte_dagsverk, mulige_dagsverk, antall_personer, arstall, kvartal "
                        + "FROM sykefravar_statistikk_naring "
                        + "where naring_kode = :naringKode "
                        + "and ("
                        + "  (arstall = :arstall and kvartal >= :kvartal) "
                        + "  or "
                        + "  (arstall > :arstall)"
                        + ") "
                        + "ORDER BY arstall, kvartal ",
                MapSqlParameterSource()
                    .addValue("naringKode", næring.tosifferIdentifikator)
                    .addValue("arstall", fraÅrstallOgKvartal.årstall)
                    .addValue("kvartal", fraÅrstallOgKvartal.kvartal)
            ) { rs: ResultSet, _: Int -> mapTilUmaskertSykefraværForEttKvartal(rs) }.sorted()
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    // TODO: fungerer ikke hvis 'fra' er eldre enn sisteKvartal.minus(3)
    fun hentUmaskertSykefraværForNorge(
        fra: ÅrstallOgKvartal
    ): List<UmaskertSykefraværForEttKvartal> {
        return try {
            try {
                namedParameterJdbcTemplate.query(
                    "SELECT tapte_dagsverk, mulige_dagsverk, antall_personer, arstall, kvartal "
                            + "FROM sykefravar_statistikk_land "
                            + "WHERE (arstall = :arstall and kvartal >= :kvartal) "
                            + "  or (arstall > :arstall) "
                            + "ORDER BY arstall, kvartal ",
                    MapSqlParameterSource()
                        .addValue("arstall", fra.årstall)
                        .addValue("kvartal", fra.kvartal)
                ) { rs: ResultSet, _: Int -> mapTilUmaskertSykefraværForEttKvartal(rs) }.sorted()
            } catch (e: EmptyResultDataAccessException) {
                emptyList()
            }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    fun hentTotaltSykefraværAlleKategorier(
        virksomhet: Virksomhet, fraÅrstallOgKvartal: ÅrstallOgKvartal
    ): Sykefraværsdata {
        val næring = virksomhet.næringskode.næring
        val maybeBransje = finnBransje(virksomhet.næringskode)
        val data: MutableMap<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> =
            EnumMap(Statistikkategori::class.java)
        data[Statistikkategori.VIRKSOMHET] = hentUmaskertSykefravær(virksomhet, fraÅrstallOgKvartal)
        data[Statistikkategori.LAND] = hentUmaskertSykefraværForNorge(fraÅrstallOgKvartal)
        if (maybeBransje.isEmpty) {
            data[Statistikkategori.NÆRING] = hentUmaskertSykefravær(næring, fraÅrstallOgKvartal)
        } else if (maybeBransje.get().erDefinertPåFemsiffernivå()) {
            data[Statistikkategori.BRANSJE] = hentUmaskertSykefravær(maybeBransje.get(), fraÅrstallOgKvartal)
        } else {
            data[Statistikkategori.BRANSJE] = hentUmaskertSykefravær(næring, fraÅrstallOgKvartal)
        }
        return Sykefraværsdata(data)
    }

    @Throws(SQLException::class)
    private fun mapTilUmaskertSykefraværForEttKvartal(rs: ResultSet): UmaskertSykefraværForEttKvartal {
        return UmaskertSykefraværForEttKvartal(
            ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
            rs.getBigDecimal("tapte_dagsverk"),
            rs.getBigDecimal("mulige_dagsverk"),
            rs.getInt("antall_personer")
        )
    }
}
