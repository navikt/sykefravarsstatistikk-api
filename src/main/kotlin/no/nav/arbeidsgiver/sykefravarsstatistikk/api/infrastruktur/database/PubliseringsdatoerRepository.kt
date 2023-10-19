package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoApi.Publiseringsdato
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.time.LocalDateTime

@Component
class PubliseringsdatoerRepository(
    @param:Qualifier("sykefravarsstatistikkJdbcTemplate") private val jdbcTemplate: NamedParameterJdbcTemplate
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    fun hentPubliseringsdatoer(): List<Publiseringsdato> {
        return try {
            jdbcTemplate.query(
                "select * from publiseringsdatoer",
                HashMap<String, Any?>()
            ) { rs: ResultSet, _: Int ->
                Publiseringsdato(
                    rs.getInt("rapport_periode"),
                    rs.getDate("offentlig_dato"),
                    rs.getDate("oppdatert_i_dvh"),
                    rs.getString("aktivitet")
                )
            }
        } catch (e: EmptyResultDataAccessException) {
            log.error("Fant ikke publiseringsdatoer i databasen, returnerer tom liste")
            emptyList()
        }
    }

    fun oppdaterPubliseringsdatoer(data: List<Publiseringsdato>) {
        val antallRadetSlettet = jdbcTemplate.update("delete from publiseringsdatoer", MapSqlParameterSource())
        log.info("Antall rader slettet fra 'publiseringsdatoer': $antallRadetSlettet")
        val antallRaderSattInn = data.stream()
            .mapToInt { (rapportPeriode, offentligDato, oppdatertDato, aktivitet): Publiseringsdato ->
                jdbcTemplate.update(
                    "insert into publiseringsdatoer "
                            + "(rapport_periode, "
                            + "offentlig_dato, "
                            + "oppdatert_i_dvh, "
                            + "aktivitet) "
                            + "values "
                            + "(:rapport_periode, "
                            + ":offentlig_dato, "
                            + ":oppdatert_i_dvh, "
                            + ":aktivitet)",
                    MapSqlParameterSource()
                        .addValue("rapport_periode", rapportPeriode)
                        .addValue("offentlig_dato", offentligDato)
                        .addValue("oppdatert_i_dvh", oppdatertDato)
                        .addValue("aktivitet", aktivitet)
                )
            }
            .sum()
        log.info("Antall rader satt inn i 'publiseringsdatoer': $antallRaderSattInn")
    }

    fun oppdaterSisteImporttidspunkt(årstallOgKvartal: ÅrstallOgKvartal) {
        log.info("Oppdaterert tidspunkt for import av sykefraværstatistikk for $årstallOgKvartal")
        val updatedRows = jdbcTemplate.update(
            "insert into importtidspunkt (aarstall, kvartal, importert) values "
                    + "(:aarstall, :kvartal, :importert)",
            MapSqlParameterSource()
                .addValue("aarstall", årstallOgKvartal.årstall)
                .addValue("kvartal", årstallOgKvartal.kvartal)
                .addValue("importert", LocalDateTime.now())
        )
        log.info("Opprettet $updatedRows rader")
    }

    fun hentSisteImporttidspunkt(): ImporttidspunktDto? {
        return jdbcTemplate
            .query(
                "select * from importtidspunkt order by importert desc fetch first 1 rows only",
            ) { resultSet: ResultSet, _: Int ->
                ImporttidspunktDto(
                    resultSet.getTimestamp("importert"),
                    ÅrstallOgKvartal(resultSet.getInt("aarstall"), resultSet.getInt("kvartal"))
                )
            }.firstOrNull()
    }
}
