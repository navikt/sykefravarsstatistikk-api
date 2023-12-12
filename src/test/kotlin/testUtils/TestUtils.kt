package testUtils

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Næring
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkLand
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.LegacyKafkaUtsendingHistorikkRepository
import org.jetbrains.exposed.sql.deleteAll
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import java.math.BigDecimal

object TestUtils {
    val PRODUKSJON_NYTELSESMIDLER = Næring("10")

    val SISTE_PUBLISERTE_KVARTAL = ÅrstallOgKvartal(2022, 1)
    fun sisteKvartalMinus(n: Int): ÅrstallOgKvartal {
        return SISTE_PUBLISERTE_KVARTAL.minusKvartaler(n)
    }


    private fun SykefravarStatistikkVirksomhetRepository.slettAlt() {
        transaction { deleteAll() }
    }

    private fun SykefraværStatistikkLandRepository.slettAlt() {
        transaction { deleteAll() }
    }

    private fun SykefraværStatistikkSektorRepository.slettAlt() {
        transaction { deleteAll() }
    }

    private fun SykefraværStatistikkNæringRepository.slettAlt() {
        transaction { deleteAll() }
    }

    private fun SykefraværStatistikkNæringskodeRepository.slettAlt() {
        transaction {
            deleteAll()
        }
    }

    private fun SykefraværStatistikkNæringskodeMedVarighetRepository.slettAlt() {
        transaction {
            deleteAll()
        }
    }

    fun slettAllStatistikkFraDatabase(
        sykefravarStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository? = null,
        sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository? = null,
        sykefraværStatistikkSektorRepository: SykefraværStatistikkSektorRepository? = null,
        sykefravarStatistikkVirksomhetGraderingRepository: SykefravarStatistikkVirksomhetGraderingRepository? = null,
        sykefraværStatistikkNæringRepository: SykefraværStatistikkNæringRepository? = null,
        sykefraværStatistikkNæringskodeRepository: SykefraværStatistikkNæringskodeRepository? = null,
        sykefraværStatistikkNæringskodeMedVarighetRepository: SykefraværStatistikkNæringskodeMedVarighetRepository? = null,
    ) {
        sykefravarStatistikkVirksomhetRepository?.slettAlt()
        sykefraværStatistikkNæringRepository?.slettAlt()
        sykefraværStatistikkNæringskodeMedVarighetRepository?.slettAlt()
        sykefravarStatistikkVirksomhetGraderingRepository?.slettAlt()
        sykefraværStatistikkNæringskodeRepository?.slettAlt()
        sykefraværStatistikkLandRepository?.slettAlt()
        sykefraværStatistikkSektorRepository?.slettAlt()
    }


    fun slettAllEksportDataFraDatabase(
        jdbcTemplate: NamedParameterJdbcTemplate? = null,
        legacyKafkaUtsendingHistorikkRepository: LegacyKafkaUtsendingHistorikkRepository
    ) {
        jdbcTemplate?.update("delete from eksport_per_kvartal", MapSqlParameterSource())
        legacyKafkaUtsendingHistorikkRepository.slettHistorikk()
        jdbcTemplate?.update(
            "delete from virksomheter_bekreftet_eksportert", MapSqlParameterSource()
        )
    }


    fun opprettTestVirksomhetMetaData(
        jdbcTemplate: NamedParameterJdbcTemplate, årstall: Int, kvartal: Int, orgnr: String?
    ) {
        opprettTestVirksomhetMetaData(jdbcTemplate, årstall, kvartal, orgnr, false)
    }


    fun opprettTestVirksomhetMetaData(
        jdbcTemplate: NamedParameterJdbcTemplate,
        årstall: Int,
        kvartal: Int,
        orgnr: String?,
        eksportert: Boolean
    ): Int {
        val parametre: SqlParameterSource = MapSqlParameterSource()
            .addValue("orgnr", orgnr)
            .addValue("årstall", årstall)
            .addValue("kvartal", kvartal)
            .addValue("eksportert", eksportert)
        return jdbcTemplate.update(
            "insert into eksport_per_kvartal "
                    + "(orgnr, arstall, kvartal, eksportert) "
                    + "values "
                    + "(:orgnr, :årstall, :kvartal, :eksportert)",
            parametre
        )
    }

    fun opprettStatistikkForLand(sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository) {
        sykefraværStatistikkLandRepository.settInn(
            listOf(
                SykefraværsstatistikkLand(
                    SISTE_PUBLISERTE_KVARTAL.årstall,
                    SISTE_PUBLISERTE_KVARTAL.kvartal,
                    10,
                    BigDecimal("4.0"),
                    BigDecimal("100.0")
                ),
                SykefraværsstatistikkLand(
                    SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).årstall,
                    SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).kvartal,
                    10,
                    BigDecimal("5.0"),
                    BigDecimal("100.0")
                ),
                SykefraværsstatistikkLand(
                    SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).årstall,
                    SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).kvartal,
                    10,
                    BigDecimal("6.0"),
                    BigDecimal("100.0")
                ),
            )
        )
    }

    fun opprettStatistikkForLandExposed(repository: SykefraværStatistikkLandRepository) {
        repository.settInn(
            listOf(
                SykefraværsstatistikkLand(
                    årstall = SISTE_PUBLISERTE_KVARTAL.årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.kvartal,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal("4.0"),
                    muligeDagsverk = BigDecimal("100.0")
                ),
                SykefraværsstatistikkLand(
                    årstall = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).kvartal,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal("5.0"),
                    muligeDagsverk = BigDecimal("100.0")
                ),
                SykefraværsstatistikkLand(
                    årstall = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).kvartal,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal("6.0"),
                    muligeDagsverk = BigDecimal("100.0")
                ),
            )
        )
    }

    fun ImporttidspunktRepository.slettAlleImporttidspunkt() {
        transaction { deleteAll() }
    }


    private fun SykefravarStatistikkVirksomhetGraderingRepository.slettAlt() {
        transaction { deleteAll() }
    }
}
