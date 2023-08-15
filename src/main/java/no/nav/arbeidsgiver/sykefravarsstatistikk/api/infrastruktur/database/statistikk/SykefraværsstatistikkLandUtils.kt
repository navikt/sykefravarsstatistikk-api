package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.statistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Sykefraværsstatistikk
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils

class SykefraværsstatistikkLandUtils(namedParameterJdbcTemplate: NamedParameterJdbcTemplate) :
    SykefraværsstatistikkIntegrasjon(namedParameterJdbcTemplate), SykefraværsstatistikkIntegrasjonUtils {
    override fun getDeleteFunction(): DeleteSykefraværsstatistikkFunction {
        return DeleteSykefraværsstatistikkFunction { (årstall, kvartal): ÅrstallOgKvartal ->
            val namedParameters: SqlParameterSource = MapSqlParameterSource()
                .addValue(ARSTALL, årstall)
                .addValue(KVARTAL, kvartal)
            val antallSlettet = namedParameterJdbcTemplate.update(
                // language=postgresql
                "delete from sykefravar_statistikk_land where arstall = :$ARSTALL and kvartal = :$KVARTAL",
                namedParameters
            )
            antallSlettet
        }
    }

    override fun getBatchCreateFunction(
        list: List<Sykefraværsstatistikk>
    ): BatchCreateSykefraværsstatistikkFunction {
        return BatchCreateSykefraværsstatistikkFunction {
            val batch =
                SqlParameterSourceUtils.createBatch(*list.toTypedArray<Sykefraværsstatistikk?>())
            namedParameterJdbcTemplate.batchUpdate(
                "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) values (:årstall, :kvartal, :antallPersoner, :tapteDagsverk, :muligeDagsverk)",
                batch
            ).sum()
        }
    }
}
