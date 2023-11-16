package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.KildeTilVirksomhetsdata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkNæringMedVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.Orgenhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.StatistikkildeDvh
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer.Publiseringsdato
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class DatavarehusRepository(
    @param:Qualifier("datavarehusJdbcTemplate") private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    private val datavarehusAggregertRepositoryV2: DatavarehusAggregertRepositoryV2,
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

    fun hentSykefraværsstatistikkNæringMedVarighet(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<SykefraværsstatistikkNæringMedVarighet> {
        val namedParameters: SqlParameterSource = MapSqlParameterSource()
            .addValue(ARSTALL, årstallOgKvartal.årstall)
            .addValue(KVARTAL, årstallOgKvartal.kvartal)
            .addValue(RECTYPE, Rectype.VIRKSOMHET.kode)
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
                resultSet.getString(VARIGHET).first(),
                resultSet.getInt(SUM_ANTALL_PERSONER),
                resultSet.getBigDecimal(SUM_TAPTE_DAGSVERK),
                resultSet.getBigDecimal(SUM_MULIGE_DAGSVERK)
            )
        }
    }

    override fun hentVirksomheter(årstallOgKvartal: ÅrstallOgKvartal): List<Orgenhet> {
        return datavarehusAggregertRepositoryV2.hentVirksomheter(årstallOgKvartal)
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
        const val NARING_5SIFFER = "naering_kode"
        const val VARIGHET = "varighet"
        const val SUM_ANTALL_PERSONER = "sum_antall_personer"
        const val SUM_TAPTE_DAGSVERK = "sum_tapte_dagsverk"
        const val SUM_MULIGE_DAGSVERK = "sum_mulige_dagsverk"
        const val RECTYPE = "rectype"
    }
}
