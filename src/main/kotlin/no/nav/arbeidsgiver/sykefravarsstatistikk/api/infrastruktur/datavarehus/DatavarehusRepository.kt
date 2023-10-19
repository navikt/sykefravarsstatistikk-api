package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.KildeTilVirksomhetsdata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor.Companion.fraSektorkode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoApi.Publiseringsdato
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering.domene.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class DatavarehusRepository(
    //public static final String RECTYPE_FOR_ORGANISASJONSLEDD = "3";
    @param:Qualifier("datavarehusJdbcTemplate") private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) : KildeTilVirksomhetsdata {
    /*
   Statistikk
  */
    fun hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
        type: StatistikkildeDvh
    ): ÅrstallOgKvartal {
        val alleÅrstallOgKvartal = namedParameterJdbcTemplate.query(
            String.format(
                "select distinct arstall, kvartal "
                        + "from %s "
                        + "order by arstall desc, kvartal desc",
                type.tabell
            ),
            MapSqlParameterSource()
        ) { resultSet: ResultSet, _: Int ->
            ÅrstallOgKvartal(
                resultSet.getInt(ARSTALL),
                resultSet.getInt(KVARTAL)
            )
        }
        return alleÅrstallOgKvartal[0]
    }

    fun hentSykefraværsstatistikkLand(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<SykefraværsstatistikkLand> {
        val namedParameters: SqlParameterSource = MapSqlParameterSource()
            .addValue(ARSTALL, årstallOgKvartal.årstall)
            .addValue(KVARTAL, årstallOgKvartal.kvartal)
        return namedParameterJdbcTemplate.query(
            "select arstall, kvartal, "
                    + "sum(antpers) as sum_antall_personer, "
                    + "sum(taptedv) as sum_tapte_dagsverk, "
                    + "sum(muligedv) as sum_mulige_dagsverk "
                    + "from dt_p.agg_ia_sykefravar_land_v "
                    + "where kjonn != 'X' and naring != 'X' "
                    + "and arstall = :arstall and kvartal = :kvartal "
                    + "group by arstall, kvartal",
            namedParameters
        ) { resultSet: ResultSet, _: Int ->
            SykefraværsstatistikkLand(
                resultSet.getInt(ARSTALL),
                resultSet.getInt(KVARTAL),
                resultSet.getInt(SUM_ANTALL_PERSONER),
                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK),
                resultSet.getBigDecimal(SUM_MULIGE_DAGSVERK)
            )
        }
    }

    fun hentSykefraværsstatistikkSektor(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<SykefraværsstatistikkSektor> {
        val namedParameters: SqlParameterSource = MapSqlParameterSource()
            .addValue(ARSTALL, årstallOgKvartal.årstall)
            .addValue(KVARTAL, årstallOgKvartal.kvartal)
        return namedParameterJdbcTemplate.query(
            "select arstall, kvartal, sektor, "
                    + "sum(antpers) as sum_antall_personer, "
                    + "sum(taptedv) as sum_tapte_dagsverk, "
                    + "sum(muligedv) as sum_mulige_dagsverk "
                    + "from dt_p.agg_ia_sykefravar_land_v "
                    + "where kjonn != 'X' and naring != 'X' "
                    + "and arstall = :arstall and kvartal = :kvartal "
                    + "group by arstall, kvartal, sektor",
            namedParameters
        ) { resultSet: ResultSet, _: Int ->
            SykefraværsstatistikkSektor(
                resultSet.getInt(ARSTALL),
                resultSet.getInt(KVARTAL),
                resultSet.getString(SEKTOR),
                resultSet.getInt(SUM_ANTALL_PERSONER),
                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK),
                resultSet.getBigDecimal(SUM_MULIGE_DAGSVERK)
            )
        }
    }

    fun hentSykefraværsstatistikkNæring(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<SykefraværsstatistikkForNæring> {
        val namedParameters: SqlParameterSource = MapSqlParameterSource()
            .addValue(ARSTALL, årstallOgKvartal.årstall)
            .addValue(KVARTAL, årstallOgKvartal.kvartal)
        return namedParameterJdbcTemplate.query(
            "select arstall, kvartal, naring, "
                    + "sum(antpers) as sum_antall_personer, "
                    + "sum(taptedv) as sum_tapte_dagsverk, "
                    + "sum(muligedv) as sum_mulige_dagsverk "
                    + "from dt_p.v_agg_ia_sykefravar_naring "
                    + "where kjonn != 'X' and naring != 'X' "
                    + "and arstall = :arstall and kvartal = :kvartal "
                    + "group by arstall, kvartal, naring",
            namedParameters
        ) { resultSet: ResultSet, _: Int ->
            SykefraværsstatistikkForNæring(
                resultSet.getInt(ARSTALL),
                resultSet.getInt(KVARTAL),
                resultSet.getString(NARING),
                resultSet.getInt(SUM_ANTALL_PERSONER),
                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK),
                resultSet.getBigDecimal(SUM_MULIGE_DAGSVERK)
            )
        }
    }

    fun hentSykefraværsstatistikkNæring5siffer(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<SykefraværsstatistikkForNæring> {
        val namedParameters: SqlParameterSource = MapSqlParameterSource()
            .addValue(ARSTALL, årstallOgKvartal.årstall)
            .addValue(KVARTAL, årstallOgKvartal.kvartal)
        return namedParameterJdbcTemplate.query(
            "select arstall, kvartal, naering_kode, "
                    + "sum(antpers) as sum_antall_personer, "
                    + "sum(taptedv) as sum_tapte_dagsverk, "
                    + "sum(muligedv) as sum_mulige_dagsverk "
                    + "from dt_p.agg_ia_sykefravar_naring_kode "
                    + "where arstall = :arstall and kvartal = :kvartal "
                    + " group by arstall, kvartal, naering_kode",
            namedParameters
        ) { resultSet: ResultSet, _: Int ->
            SykefraværsstatistikkForNæring(
                resultSet.getInt(ARSTALL),
                resultSet.getInt(KVARTAL),
                resultSet.getString(NARING_5SIFFER),
                resultSet.getInt(SUM_ANTALL_PERSONER),
                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK),
                resultSet.getBigDecimal(SUM_MULIGE_DAGSVERK)
            )
        }
    }

    fun hentSykefraværsstatistikkVirksomhet(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<SykefraværsstatistikkVirksomhet> {
        val namedParameters: SqlParameterSource = MapSqlParameterSource()
            .addValue(ARSTALL, årstallOgKvartal.årstall)
            .addValue(KVARTAL, årstallOgKvartal.kvartal)
        return namedParameterJdbcTemplate.query(
            "select arstall, kvartal, orgnr, varighet, rectype, "
                    + "sum(antpers) as sum_antall_personer, "
                    + "sum(taptedv) as sum_tapte_dagsverk, "
                    + "sum(muligedv) as sum_mulige_dagsverk "
                    + "from dt_p.agg_ia_sykefravar_v "
                    + "where arstall = :arstall and kvartal = :kvartal "
                    + "group by arstall, kvartal, orgnr, varighet, rectype",
            namedParameters
        ) { resultSet: ResultSet, _: Int ->
            SykefraværsstatistikkVirksomhet(
                resultSet.getInt(ARSTALL),
                resultSet.getInt(KVARTAL),
                resultSet.getString(ORGNR),
                resultSet.getString(VARIGHET),
                resultSet.getString(RECTYPE),
                resultSet.getInt(SUM_ANTALL_PERSONER),
                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK),
                resultSet.getBigDecimal(SUM_MULIGE_DAGSVERK)
            )
        }
    }

    fun hentSykefraværsstatistikkNæringMedVarighet(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<SykefraværsstatistikkNæringMedVarighet> {
        val namedParameters: SqlParameterSource = MapSqlParameterSource()
            .addValue(ARSTALL, årstallOgKvartal.årstall)
            .addValue(KVARTAL, årstallOgKvartal.kvartal)
            .addValue(RECTYPE, RECTYPE_FOR_VIRKSOMHET)
        return namedParameterJdbcTemplate.query(
            "select arstall, kvartal, naering_kode, varighet, "
                    + "sum(antpers) as sum_antall_personer, "
                    + "sum(taptedv) as sum_tapte_dagsverk, "
                    + "sum(muligedv) as sum_mulige_dagsverk "
                    + "from dt_p.agg_ia_sykefravar_v "
                    + "where arstall = :arstall and kvartal = :kvartal and varighet is not null "
                    + "and rectype= :rectype "
                    + "group by arstall, kvartal, naering_kode, varighet",
            namedParameters
        ) { resultSet: ResultSet, _: Int ->
            SykefraværsstatistikkNæringMedVarighet(
                resultSet.getInt(ARSTALL),
                resultSet.getInt(KVARTAL),
                resultSet.getString(NARING_5SIFFER),
                resultSet.getString(VARIGHET),
                resultSet.getInt(SUM_ANTALL_PERSONER),
                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK),
                resultSet.getBigDecimal(SUM_MULIGE_DAGSVERK)
            )
        }
    }

    fun hentSykefraværsstatistikkVirksomhetMedGradering(årstallOgKvartal: ÅrstallOgKvartal): List<SykefraværsstatistikkVirksomhetMedGradering> {
        val namedParameters: SqlParameterSource = MapSqlParameterSource()
            .addValue(ARSTALL, årstallOgKvartal.årstall)
            .addValue(KVARTAL, årstallOgKvartal.kvartal)
        return namedParameterJdbcTemplate.query(
            "select arstall, kvartal, orgnr, naring, naering_kode, rectype, "
                    + "sum(taptedv_gs) as sum_tapte_dagsverk_gs, "
                    + "sum(antall_gs) as sum_antall_graderte_sykemeldinger, "
                    + "sum(antall) as sum_antall_sykemeldinger, "
                    + "sum(antpers) as sum_antall_personer, "
                    + "sum(taptedv) as sum_tapte_dagsverk, "
                    + "sum(mulige_dv) as sum_mulige_dagsverk "
                    + "from dt_p.agg_ia_sykefravar_v_2 "
                    + "where arstall = :arstall and kvartal = :kvartal "
                    + "group by arstall, kvartal, orgnr, naring, naering_kode, rectype",
            namedParameters
        ) { resultSet: ResultSet, _: Int ->
            SykefraværsstatistikkVirksomhetMedGradering(
                resultSet.getInt(ARSTALL),
                resultSet.getInt(KVARTAL),
                resultSet.getString(ORGNR),
                resultSet.getString(NARING),
                resultSet.getString(NARING_5SIFFER),
                resultSet.getString(RECTYPE),
                resultSet.getInt(SUM_ANTALL_GRADERTE_SYKEMELDINGER),
                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK_GS),
                resultSet.getInt(SUM_ANTALL_SYKEMELDINGER),
                resultSet.getInt(SUM_ANTALL_PERSONER),
                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK),
                resultSet.getBigDecimal(SUM_MULIGE_DAGSVERK)
            )
        }
    }

    override fun hentVirksomheter(årstallOgKvartal: ÅrstallOgKvartal): List<Orgenhet> {
        val namedParameters = MapSqlParameterSource()
            .addValue(ARSTALL, årstallOgKvartal.årstall)
            .addValue(KVARTAL, årstallOgKvartal.kvartal)
        return namedParameterJdbcTemplate.query(
            "select distinct orgnr, rectype, sektor, substr(primærnæringskode, 1,2) as naring, primærnæringskode as naringskode, arstall, kvartal "
                    + "from dt_p.agg_ia_sykefravar_v_2 "
                    + "where arstall = :arstall and kvartal = :kvartal "
                    + "and length(trim(orgnr)) = 9 "
                    + "and primærnæringskode is not null "
                    + "and primærnæringskode != '00.000'",
            namedParameters
        ) { resultSet: ResultSet, _: Int ->
            Orgenhet(
                Orgnr(resultSet.getString(ORGNR)),
                "",  // henter ikke lenger navn for virksomheter, da dette ikke er i bruk
                resultSet.getString(RECTYPE),
                fraSektorkode(resultSet.getString(SEKTOR)),
                resultSet.getString(NARING),
                resultSet.getString("naringskode").replace(".", ""),
                ÅrstallOgKvartal(resultSet.getInt(ARSTALL), resultSet.getInt(KVARTAL))
            )
        }
    }

    fun hentPubliseringsdatoerFraDvh(): List<Publiseringsdato> {
        return try {
            namedParameterJdbcTemplate.query(
                "select rapport_periode, offentlig_dato, oppdatert_dato, aktivitet "
                        + "from dk_p.publiseringstabell "
                        + "where TABELL_NAVN = 'AGG_FAK_SYKEFRAVAR_DIA' "
                        + "and PERIODE_TYPE = 'KVARTAL' "
                        + "order by offentlig_dato desc",
                HashMap<String, Any?>()
            ) { rs: ResultSet, _: Int ->
                Publiseringsdato(
                    rs.getInt("rapport_periode"),
                    rs.getDate("offentlig_dato").toLocalDate(),
                    rs.getDate("oppdatert_dato").toLocalDate(),
                    rs.getString("aktivitet")
                )
            }
        } catch (e: EmptyResultDataAccessException) {
            emptyList()
        }
    }

    companion object {
        const val ARSTALL = "arstall"
        const val KVARTAL = "kvartal"
        const val SEKTOR = "sektor"
        const val NARING = "naring"
        const val NARING_5SIFFER = "naering_kode"
        const val ORGNR = "orgnr"
        const val VARIGHET = "varighet"
        const val SUM_TAPTE_DAGSVERK_GS = "sum_tapte_dagsverk_gs"
        const val SUM_ANTALL_PERSONER = "sum_antall_personer"
        const val SUM_TAPTE_DAGSVERK = "sum_tapte_dagsverk"
        const val SUM_MULIGE_DAGSVERK = "sum_mulige_dagsverk"
        const val SUM_ANTALL_GRADERTE_SYKEMELDINGER = "sum_antall_graderte_sykemeldinger"
        const val SUM_ANTALL_SYKEMELDINGER = "sum_antall_sykemeldinger"
        const val RECTYPE = "rectype"
        const val RECTYPE_FOR_FORETAK = "1"
        const val RECTYPE_FOR_VIRKSOMHET = "2"
    }
}
