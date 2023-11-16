package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer.Publiseringsdato
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.EmptyResultDataAccessException
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
    fun hentSisteÅrstallOgKvartalForSykefraværsstatistikk(): ÅrstallOgKvartal {
        return datavarehusAggregertRepositoryV1.hentSisteKvartal()
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
    }
}
