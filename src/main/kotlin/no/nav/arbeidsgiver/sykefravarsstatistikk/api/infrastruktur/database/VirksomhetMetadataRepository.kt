package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.VirksomhetMetadata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.VirksomhetMetadataMedNæringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Næringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.jetbrains.exposed.sql.*
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
    @param:Qualifier("sykefravarsstatistikkJdbcTemplate") private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    override val database: Database,
) : UsingExposed, Table("virksomhet_metadata") {
    private val orgnr = text("orgnr")
    private val navn = text("navn")
    private val rectype = varchar("rectype", 1)
    private val sektor = text("sektor")
    private val primærnæring = varchar("primarnaring", 2)
    private val primærnæringskode = varchar("primarnaringskode", 5)
    private val arstall = integer("arstall")
    private val kvartal = integer("kvartal")

    private val log = LoggerFactory.getLogger(this::class.java)

    open fun opprettVirksomhetMetadata(virksomhetMetadata: List<VirksomhetMetadata>): Int {
        return transaction {
            batchInsert(virksomhetMetadata, shouldReturnGeneratedValues = false) {
                this[orgnr] = it.orgnr
                this[navn] = it.navn
                this[rectype] = it.rectype
                this[sektor] = it.sektor.sektorkode
                this[primærnæring] = it.primærnæring
                this[primærnæringskode] = it.primærnæringskode
                this[arstall] = it.årstall
                this[kvartal] = it.kvartal
            }.count()
        }
    }

    @Deprecated("Brukes kun av legacy Kafka-strøm, som skal fases ut.")
    open fun opprettVirksomhetMetadataNæringskode5siffer(
        virksomhetMetadataMedNæringskode: List<VirksomhetMetadataMedNæringskode>
    ): Int {
        val batch = SqlParameterSourceUtils.createBatch(*virksomhetMetadataMedNæringskode.toTypedArray())
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

    @Deprecated("Brukes kun av legacy Kafka-strøm, som skal fases ut.")
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

    open fun hentVirksomhetMetadata(årstallOgKvartal: ÅrstallOgKvartal): List<VirksomhetMetadata> {
        return transaction {
            select {
                (arstall eq årstallOgKvartal.årstall) and (kvartal eq årstallOgKvartal.kvartal)
            }.map {
                VirksomhetMetadata(
                    Orgnr(it[orgnr]),
                    it[navn],
                    it[rectype],
                    Sektor.fraSektorkode(it[sektor])!!,
                    it[primærnæring],
                    it[primærnæringskode],
                    ÅrstallOgKvartal(
                        it[arstall], it[kvartal]
                    )
                )
            }
        }
    }

    open fun slettVirksomhetMetadata(): Int = transaction {
        deleteAll()
    }

    @Deprecated("Brukes kun av legacy Kafka-strøm, som skal fases ut.")
    open fun slettNæringOgNæringskode5siffer(): Int =
        namedParameterJdbcTemplate.update(
            "delete from virksomhet_metadata_naring_kode_5siffer", MapSqlParameterSource()
        )

    private fun assemble(
        virksomhetMetadata: List<VirksomhetMetadata>,
        næringOgNæringskode5siffer: List<Pair<Orgnr, Næringskode>>
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
            Næringskode(resultSet.getString("naring_kode_5siffer"))
        )
    }
}
