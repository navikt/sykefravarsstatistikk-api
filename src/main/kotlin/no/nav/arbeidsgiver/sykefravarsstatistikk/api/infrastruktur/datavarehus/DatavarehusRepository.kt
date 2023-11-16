package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkNæringMedVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.StatistikkildeDvh
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer.Publiseringsdato
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
class DatavarehusRepository(
    @param:Qualifier("datavarehusJdbcTemplate") private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    private val datavarehusAggregertRepositoryV1: DatavarehusAggregertRepositoryV1,
) {
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
        return datavarehusAggregertRepositoryV1.hentSykefraværsstatistikkNæringMedVarighet(årstallOgKvartal)
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
