package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Virksomhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram.finnBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

@Component
class VarighetRepository(
    @param:Qualifier("sykefravarsstatistikkJdbcTemplate") private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {
    fun hentSykefraværMedVarighet(
        næring: Næring
    ): List<UmaskertSykefraværForEttKvartalMedVarighet> {
        return try {
            namedParameterJdbcTemplate.query(
                "select tapte_dagsverk, mulige_dagsverk, antall_personer, varighet, arstall, kvartal "
                        + " from sykefravar_statistikk_naring_med_varighet "
                        + " where "
                        + " naring_kode like :næring "
                        + " and varighet in ('A', 'B', 'C', 'D', 'E', 'F', 'X')"
                        + " order by arstall, kvartal, varighet",
                MapSqlParameterSource().addValue("næring", "${næring.kode.substring(0, 2)}%")
            ) { rs: ResultSet, _: Int -> mapTilKvartalsvisSykefraværMedVarighet(rs) }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    fun hentSykefraværMedVarighet(
        bransje: Bransje
    ): List<UmaskertSykefraværForEttKvartalMedVarighet> {
        return try {
            namedParameterJdbcTemplate.query(
                "select tapte_dagsverk, mulige_dagsverk, antall_personer, varighet, arstall, kvartal "
                        + " from sykefravar_statistikk_naring_med_varighet "
                        + " where "
                        + " naring_kode in (:naringKoder) "
                        + " and varighet in ('A', 'B', 'C', 'D', 'E', 'F', 'X')"
                        + " order by arstall, kvartal, varighet",
                MapSqlParameterSource()
                    .addValue("naringKoder", bransje.koderSomSpesifisererNæringer)
            ) { rs: ResultSet, _: Int -> mapTilKvartalsvisSykefraværMedVarighet(rs) }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    fun hentSykefraværMedVarighet(
        virksomhet: Virksomhet
    ): List<UmaskertSykefraværForEttKvartalMedVarighet> {
        return try {
            namedParameterJdbcTemplate.query(
                "select tapte_dagsverk, mulige_dagsverk, antall_personer, varighet, arstall, kvartal "
                        + " from sykefravar_statistikk_virksomhet "
                        + " where "
                        + " orgnr = :orgnr "
                        + " and varighet in ('A', 'B', 'C', 'D', 'E', 'F', 'X')"
                        + " order by arstall, kvartal, varighet",
                MapSqlParameterSource().addValue("orgnr", virksomhet.orgnr.verdi)
            ) { rs: ResultSet, _: Int -> mapTilKvartalsvisSykefraværMedVarighet(rs) }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    fun hentUmaskertSykefraværMedVarighetAlleKategorier(virksomhet: Virksomhet): Map<Statistikkategori, List<UmaskertSykefraværForEttKvartalMedVarighet>> {
        val næring = Næring(virksomhet.næringskode!!.kode, "")
        val maybeBransje = finnBransje(virksomhet.næringskode)
        val data: MutableMap<Statistikkategori, List<UmaskertSykefraværForEttKvartalMedVarighet>> = EnumMap(Statistikkategori::class.java)
        data[Statistikkategori.VIRKSOMHET] = hentSykefraværMedVarighet(virksomhet)
        if (maybeBransje.isEmpty) {
            data[Statistikkategori.NÆRING] = hentSykefraværMedVarighet(næring)
        } else if (maybeBransje.get().erDefinertPåFemsiffernivå()) {
            data[Statistikkategori.BRANSJE] = hentSykefraværMedVarighet(maybeBransje.get())
        } else {
            data[Statistikkategori.BRANSJE] = hentSykefraværMedVarighet(næring)
        }
        return data
    }

    @Throws(SQLException::class)
    private fun mapTilKvartalsvisSykefraværMedVarighet(
        rs: ResultSet
    ): UmaskertSykefraværForEttKvartalMedVarighet {
        return UmaskertSykefraværForEttKvartalMedVarighet(
            ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
            rs.getBigDecimal("tapte_dagsverk"),
            rs.getBigDecimal("mulige_dagsverk"),
            rs.getInt("antall_personer"),
            Varighetskategori.fraKode(rs.getString("varighet"))
        )
    }
}
