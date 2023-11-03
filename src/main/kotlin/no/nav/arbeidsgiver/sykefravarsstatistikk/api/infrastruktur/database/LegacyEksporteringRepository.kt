package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.VirksomhetEksportPerKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.*

@Component
@Deprecated("Slettes når Salesforce-teamet har gått over til eksport per kategori")
class LegacyEksporteringRepository(
    @param:Qualifier("sykefravarsstatistikkJdbcTemplate") private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {
    fun opprettEksport(virksomhetEksportPerKvartalList: List<VirksomhetEksportPerKvartal?>?): Int {
        if (virksomhetEksportPerKvartalList.isNullOrEmpty()) {
            return 0
        }
        val batch = SqlParameterSourceUtils.createBatch(*virksomhetEksportPerKvartalList.toTypedArray())
        val results = namedParameterJdbcTemplate.batchUpdate(
            "insert into eksport_per_kvartal "
                    + "(orgnr, arstall, kvartal) "
                    + "values "
                    + "(:orgnr, :årstall, :kvartal)",
            batch
        )
        return Arrays.stream(results).sum()
    }

    fun hentVirksomhetEksportPerKvartal(
        årstallOgKvartal: ÅrstallOgKvartal
    ): List<VirksomhetEksportPerKvartal> {
        val parametre: SqlParameterSource = MapSqlParameterSource()
            .addValue("årstall", årstallOgKvartal.årstall)
            .addValue("kvartal", årstallOgKvartal.kvartal)
        return namedParameterJdbcTemplate.query(
            "select orgnr, arstall, kvartal, eksportert "
                    + "from eksport_per_kvartal "
                    + "where arstall = :årstall "
                    + "and kvartal = :kvartal",
            parametre
        ) { resultSet: ResultSet, _: Int ->
            VirksomhetEksportPerKvartal(
                Orgnr(resultSet.getString("orgnr")),
                ÅrstallOgKvartal(resultSet.getInt("arstall"), resultSet.getInt("kvartal")),
                resultSet.getBoolean("eksportert")
            )
        }
    }

    fun batchOpprettVirksomheterBekreftetEksportert(
        virksomheterSomErBekreftetEksportert: List<String?>, årstallOgKvartal: ÅrstallOgKvartal
    ): Int {
        val virksomheter = virksomheterSomErBekreftetEksportert.stream()
            .map { orgnr: String? ->
                BatchUpdateVirksomhetTilEksport(
                    orgnr,
                    årstallOgKvartal.årstall,
                    årstallOgKvartal.kvartal,
                    true,
                    LocalDateTime.now()
                )
            }
            .toList()
        val batch = SqlParameterSourceUtils.createBatch(*virksomheter.toTypedArray())
        val results = namedParameterJdbcTemplate.batchUpdate(
            "insert into virksomheter_bekreftet_eksportert "
                    + "(orgnr, arstall, kvartal) "
                    + "values "
                    + "(:orgnr, :årstall, :kvartal)",
            batch
        )
        return Arrays.stream(results).sum()
    }

    fun oppdaterAlleVirksomheterIEksportTabellSomErBekrreftetEksportert(): Int {
        val parametre: SqlParameterSource = MapSqlParameterSource().addValue("oppdatert", LocalDateTime.now())
        return namedParameterJdbcTemplate.update(
            "update eksport_per_kvartal set eksportert = true, oppdatert = :oppdatert "
                    + "where orgnr in (select orgnr from virksomheter_bekreftet_eksportert) "
                    + "and eksportert = false ",
            parametre
        )
    }

    fun hentAntallIkkeFerdigEksportert(): Int {
        val parametre: SqlParameterSource = MapSqlParameterSource().addValue("eksportert", false)
        return namedParameterJdbcTemplate.queryForObject(
            "select count(*) from eksport_per_kvartal " + "where eksportert = :eksportert ",
            parametre,
            Int::class.java
        )!!
    }

    fun slettEksportertPerKvartal(): Int {
        val parametre: SqlParameterSource = MapSqlParameterSource()
        return namedParameterJdbcTemplate.update("delete from eksport_per_kvartal", parametre)
    }

    fun slettVirksomheterBekreftetEksportert(): Int {
        val parametre: SqlParameterSource = MapSqlParameterSource()
        return namedParameterJdbcTemplate.update(
            "delete from virksomheter_bekreftet_eksportert", parametre
        )
    }

    private data class BatchUpdateVirksomhetTilEksport(
        val orgnr: String?,
        val årstall: Int,
        val kvartal: Int,
        val eksportert: Boolean,
        val oppdatert: LocalDateTime
    )
}
