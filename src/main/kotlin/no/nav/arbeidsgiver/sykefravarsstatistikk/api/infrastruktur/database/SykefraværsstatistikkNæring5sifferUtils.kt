package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.aggregert.Sykefraværsstatistikk
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.ÅrstallOgKvartal
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils
import java.util.*

class SykefraværsstatistikkNæring5sifferUtils(
    namedParameterJdbcTemplate: NamedParameterJdbcTemplate?
) : SykefraværsstatistikkIntegrasjon(namedParameterJdbcTemplate!!), SykefraværsstatistikkIntegrasjonUtils {
    override fun getDeleteFunction(): DeleteSykefraværsstatistikkFunction {
        return DeleteSykefraværsstatistikkFunction { (årstall, kvartal): ÅrstallOgKvartal ->
            val namedParameters: SqlParameterSource = MapSqlParameterSource()
                .addValue(ARSTALL, årstall)
                .addValue(KVARTAL, kvartal)
            val antallSlettet = namedParameterJdbcTemplate.update(
                String.format(
                    "delete from sykefravar_statistikk_naring5siffer where arstall = :%s and kvartal = :%s",
                    ARSTALL, KVARTAL
                ),
                namedParameters
            )
            antallSlettet
        }
    }

    override fun getBatchCreateFunction(statistikk: List<Sykefraværsstatistikk>): BatchCreateSykefraværsstatistikkFunction {
        return BatchCreateSykefraværsstatistikkFunction {
            val batch =
                SqlParameterSourceUtils.createBatch(*statistikk.toTypedArray<Sykefraværsstatistikk?>())
            val results = namedParameterJdbcTemplate.batchUpdate(
                "insert into sykefravar_statistikk_naring5siffer "
                        + "(arstall, kvartal, naring_kode, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "values "
                        + "(:årstall, :kvartal, :næringkode, :antallPersoner, :tapteDagsverk, :muligeDagsverk)",
                batch
            )
            Arrays.stream(results).sum()
        }
    }
}
