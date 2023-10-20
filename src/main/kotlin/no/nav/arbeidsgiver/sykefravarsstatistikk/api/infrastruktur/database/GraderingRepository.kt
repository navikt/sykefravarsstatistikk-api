package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Sykefraværsdata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.VirksomhetMetadataMedNæringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Bransjeprogram.finnBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

@Component
class GraderingRepository(
    @param:Qualifier("sykefravarsstatistikkJdbcTemplate") private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {
    fun hentVirksomhetMetadataNæringskode5siffer(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<VirksomhetMetadataMedNæringskode> {
        return try {
            namedParameterJdbcTemplate.query(
                "select arstall, kvartal, orgnr, naring, naring_kode"
                        + " from sykefravar_statistikk_virksomhet_med_gradering "
                        + " where "
                        + " arstall = :årstall "
                        + " and kvartal = :kvartal "
                        + " group by arstall, kvartal, orgnr, naring, naring_kode"
                        + " order by arstall, kvartal, orgnr, naring, naring_kode",
                MapSqlParameterSource()
                    .addValue("årstall", årstallOgKvartal.årstall)
                    .addValue("kvartal", årstallOgKvartal.kvartal)
            ) { rs: ResultSet, _: Int ->
                VirksomhetMetadataMedNæringskode(
                    Orgnr(rs.getString("orgnr")),
                    ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
                    Næringskode(rs.getString("naring_kode"))
                )
            }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    fun hentSykefraværMedGradering(virksomhet: Virksomhet): List<UmaskertSykefraværForEttKvartal> {
        return try {
            namedParameterJdbcTemplate.query(
                "select arstall, kvartal,"
                        + " sum(tapte_dagsverk_gradert_sykemelding) as "
                        + "sum_tapte_dagsverk_gradert_sykemelding, "
                        + " sum(tapte_dagsverk) as sum_tapte_dagsverk, "
                        + " sum(antall_personer) as sum_antall_personer "
                        + " from sykefravar_statistikk_virksomhet_med_gradering "
                        + " where "
                        + " orgnr = :orgnr "
                        + " and rectype = :rectype "
                        + " group by arstall, kvartal"
                        + " order by arstall, kvartal",
                MapSqlParameterSource()
                    .addValue("orgnr", virksomhet.orgnr.verdi)
                    .addValue("rectype", DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET)
            ) { rs: ResultSet, _: Int -> mapTilUmaskertSykefraværForEttKvartal(rs) }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    fun hentGradertSykefraværAlleKategorier(virksomhet: Virksomhet): Sykefraværsdata {
        val næring = virksomhet.næringskode.næring
        val maybeBransje = finnBransje(virksomhet.næringskode)
        val data: MutableMap<Statistikkategori, List<UmaskertSykefraværForEttKvartal>> = EnumMap(Statistikkategori::class.java)
        data[Statistikkategori.VIRKSOMHET] = hentSykefraværMedGradering(virksomhet)
        if (maybeBransje.isEmpty) {
            data[Statistikkategori.NÆRING] = hentSykefraværMedGradering(næring)
        } else if (maybeBransje.get().erDefinertPåFemsiffernivå()) {
            data[Statistikkategori.BRANSJE] = hentSykefraværMedGradering(maybeBransje.get())
        } else {
            data[Statistikkategori.BRANSJE] = hentSykefraværMedGradering(næring)
        }
        return Sykefraværsdata(data)
    }

    fun hentSykefraværMedGradering(næring: Næring): List<UmaskertSykefraværForEttKvartal> {
        return try {
            namedParameterJdbcTemplate.query(
                "select arstall, kvartal,"
                        + " sum(tapte_dagsverk_gradert_sykemelding) as "
                        + "sum_tapte_dagsverk_gradert_sykemelding, "
                        + " sum(tapte_dagsverk) as sum_tapte_dagsverk, "
                        + " sum(antall_personer) as sum_antall_personer "
                        + " from sykefravar_statistikk_virksomhet_med_gradering "
                        + " where "
                        + " naring = :naring "
                        + " and rectype = :rectype "
                        + " group by arstall, kvartal"
                        + " order by arstall, kvartal",
                MapSqlParameterSource()
                    .addValue("naring", næring.tosifferIdentifikator)
                    .addValue("rectype", DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET)
            ) { rs: ResultSet, _: Int -> mapTilUmaskertSykefraværForEttKvartal(rs) }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    fun hentSykefraværMedGradering(bransje: Bransje): List<UmaskertSykefraværForEttKvartal> {
        return try {
            namedParameterJdbcTemplate.query(
                "select arstall, kvartal,"
                        + " sum(tapte_dagsverk_gradert_sykemelding) as "
                        + "sum_tapte_dagsverk_gradert_sykemelding, "
                        + " sum(tapte_dagsverk) as sum_tapte_dagsverk, "
                        + " sum(antall_personer) as sum_antall_personer "
                        + " from sykefravar_statistikk_virksomhet_med_gradering "
                        + " where "
                        + " naring_kode in (:naringKoder) "
                        + " and rectype = :rectype "
                        + " group by arstall, kvartal"
                        + " order by arstall, kvartal",
                MapSqlParameterSource()
                    .addValue("naringKoder", bransje.identifikatorer)
                    .addValue("rectype", DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET)
            ) { rs: ResultSet, _: Int -> mapTilUmaskertSykefraværForEttKvartal(rs) }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    @Throws(SQLException::class)
    private fun mapTilUmaskertSykefraværForEttKvartal(rs: ResultSet): UmaskertSykefraværForEttKvartal {
        return UmaskertSykefraværForEttKvartal(
            ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
            rs.getBigDecimal("sum_tapte_dagsverk_gradert_sykemelding"),
            rs.getBigDecimal("sum_tapte_dagsverk"),
            rs.getInt("sum_antall_personer")
        )
    }
}
