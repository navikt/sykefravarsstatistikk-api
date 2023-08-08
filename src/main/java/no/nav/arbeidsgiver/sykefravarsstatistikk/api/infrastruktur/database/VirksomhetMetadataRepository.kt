package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.NæringOgNæringskode5siffer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.VirksomhetMetadata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.VirksomhetMetadataNæringskode5siffer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell.ÅrstallOgKvartal
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils
import org.springframework.stereotype.Component
import java.sql.ResultSet

@Component
open class VirksomhetMetadataRepository(
    @param:Qualifier("sykefravarsstatistikkJdbcTemplate") private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    open fun opprettVirksomhetMetadata(virksomhetMetadata: List<VirksomhetMetadata>): Int {
        val batch = SqlParameterSourceUtils.createBatch(*virksomhetMetadata.toTypedArray())
        val results = namedParameterJdbcTemplate.batchUpdate(
            """
                |insert into virksomhet_metadata
                |    (orgnr, navn, rectype, sektor, primarnaring, primarnaringskode, arstall, kvartal)
                |values
                |    (:orgnr, :navn, :rectype, :sektor, :primærnæring, :primærnæringskode, :årstall, :kvartal)
            """.trimMargin(),
            batch,
        )
        return results.sum()
    }

    open fun opprettVirksomhetMetadataNæringskode5siffer(
        virksomhetMetadataNæringskode5siffer: List<VirksomhetMetadataNæringskode5siffer>
    ): Int {
        val batch = SqlParameterSourceUtils.createBatch(*virksomhetMetadataNæringskode5siffer.toTypedArray())
        val results = namedParameterJdbcTemplate.batchUpdate(
            """
                |insert into virksomhet_metadata_naring_kode_5siffer
                |    (orgnr, naring_kode, naring_kode_5siffer, arstall, kvartal)
                |values
                |    (:orgnr, :næring, :næringskode5siffer, :årstall, :kvartal)
            """.trimMargin(),
            batch
        )
        return results.sum()
    }

    open fun hentVirksomhetMetadataMedNæringskoder(årstallOgKvartal: ÅrstallOgKvartal): List<VirksomhetMetadata> {
        val virksomhetMetadata = hentVirksomhetMetadata(årstallOgKvartal)

        log.info("Henter data fra 'virksomhet_metadata_naring_kode_5siffer' for $årstallOgKvartal ...")
        val paramSource = MapSqlParameterSource()
            .addValue("årstall", årstallOgKvartal.årstall)
            .addValue("kvartal", årstallOgKvartal.kvartal)
        val næringOgNæringskode5siffer = namedParameterJdbcTemplate.query(
            """
                |select orgnr, naring_kode, naring_kode_5siffer
                |from virksomhet_metadata_naring_kode_5siffer
                |where arstall = :årstall and kvartal = :kvartal
            """.trimMargin(),
            paramSource,
            næringOgNæringskode5sifferRowMapper()
        )
        log.info("Datauthenting fra 'virksomhet_metadata_naring_kode_5siffer' ferdig.")
        return assemble(virksomhetMetadata, næringOgNæringskode5siffer)
    }

     open fun hentVirksomhetMetadata(årstallOgKvartal: ÅrstallOgKvartal): MutableList<VirksomhetMetadata> {
        val paramSource = MapSqlParameterSource()
            .addValue("årstall", årstallOgKvartal.årstall)
            .addValue("kvartal", årstallOgKvartal.kvartal)
        log.info("Henter data fra 'virksomhet_metadata' for $årstallOgKvartal ...")
        val virksomhetMetadata = namedParameterJdbcTemplate.query(
            """
                |select orgnr, navn, rectype, sektor, primarnaring, primarnaringskode, arstall, kvartal
                |from virksomhet_metadata
                |where arstall = :årstall and kvartal = :kvartal
                """.trimMargin(),
            paramSource,
            virksomhetMetadataRowMapper(),
        )
        log.info("Datauthenting fra 'virksomhet_metadata' ferdig.")
        return virksomhetMetadata
    }

    open fun slettVirksomhetMetadata(): Int =
        namedParameterJdbcTemplate.update("delete from virksomhet_metadata", MapSqlParameterSource())

    open fun slettNæringOgNæringskode5siffer(): Int =
        namedParameterJdbcTemplate.update(
            "delete from virksomhet_metadata_naring_kode_5siffer", MapSqlParameterSource()
        )

    private fun assemble(
        virksomhetMetadata: List<VirksomhetMetadata>,
        næringOgNæringskode5siffer: List<Pair<Orgnr, NæringOgNæringskode5siffer>>
    ): List<VirksomhetMetadata> {

        val map = næringOgNæringskode5siffer
            .groupBy({ it.first }, { it.second })

        virksomhetMetadata.forEach {
            it.leggTilNæringOgNæringskode5siffer(map[Orgnr(it.orgnr)])
        }

        return virksomhetMetadata
    }

    private fun næringOgNæringskode5sifferRowMapper() = RowMapper { resultSet: ResultSet, _: Int ->
        Pair(
            Orgnr(resultSet.getString("orgnr")),
            NæringOgNæringskode5siffer(
                resultSet.getString("naring_kode"),
                resultSet.getString("naring_kode_5siffer")
            )
        )
    }

    private fun virksomhetMetadataRowMapper() = RowMapper { resultSet: ResultSet, _: Int ->
        VirksomhetMetadata(
            Orgnr(resultSet.getString("orgnr")),
            resultSet.getString("navn"),
            resultSet.getString("rectype"),
            resultSet.getString("sektor"),
            resultSet.getString("primarnaring"),
            resultSet.getString("primarnaringskode"),
            ÅrstallOgKvartal(
                resultSet.getInt("arstall"), resultSet.getInt("kvartal")
            )
        )
    }
}
