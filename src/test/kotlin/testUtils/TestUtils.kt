package testUtils

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaUtsendingHistorikkData
import org.jetbrains.exposed.sql.deleteAll
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import java.math.BigDecimal
import java.sql.ResultSet

object TestUtils {
    val PRODUKSJON_NYTELSESMIDLER = Næring("10")

    val SISTE_PUBLISERTE_KVARTAL = ÅrstallOgKvartal(2022, 1)
    fun sisteKvartalMinus(n: Int): ÅrstallOgKvartal {
        return SISTE_PUBLISERTE_KVARTAL.minusKvartaler(n)
    }


    fun parametreForStatistikk(
        årstall: Int, kvartal: Int, antallPersoner: Int, tapteDagsverk: Int, muligeDagsverk: Int
    ): MapSqlParameterSource {
        return MapSqlParameterSource()
            .addValue("arstall", årstall)
            .addValue("kvartal", kvartal)
            .addValue("antall_personer", antallPersoner)
            .addValue("tapte_dagsverk", tapteDagsverk)
            .addValue("mulige_dagsverk", muligeDagsverk)
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

    fun slettAllStatistikkFraDatabase(
        jdbcTemplate: NamedParameterJdbcTemplate,
        sykefravarStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository? = null,
        sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository? = null,
        sykefraværStatistikkSektorRepository: SykefraværStatistikkSektorRepository? = null,
        sykefravarStatistikkVirksomhetGraderingRepository: SykefravarStatistikkVirksomhetGraderingRepository? = null,
        sykefraværStatistikkNæringRepository: SykefraværStatistikkNæringRepository? = null,
    ) {
        sykefravarStatistikkVirksomhetRepository?.slettAlt()
        sykefraværStatistikkNæringRepository?.slettAlt()
        jdbcTemplate.update(
            "delete from sykefravar_statistikk_naring_med_varighet", MapSqlParameterSource()
        )
        sykefravarStatistikkVirksomhetGraderingRepository?.slettAlt()
        jdbcTemplate.update(
            "delete from sykefravar_statistikk_naring5siffer", MapSqlParameterSource()
        )
        sykefraværStatistikkLandRepository?.slettAlt()
        sykefraværStatistikkSektorRepository?.slettAlt()
    }


    fun slettAllEksportDataFraDatabase(jdbcTemplate: NamedParameterJdbcTemplate) {
        jdbcTemplate.update("delete from virksomhet_metadata", MapSqlParameterSource())
        jdbcTemplate.update("delete from eksport_per_kvartal", MapSqlParameterSource())
        jdbcTemplate.update("delete from kafka_utsending_historikk", MapSqlParameterSource())
        jdbcTemplate.update(
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
        val statistikk = listOf(
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
        sykefraværStatistikkLandRepository.settInn(statistikk)
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

    fun opprettStatistikkForNæringskode(
        jdbcTemplate: NamedParameterJdbcTemplate,
        næringskode5Siffer: Næringskode,
        årstall: Int,
        kvartal: Int,
        tapteDagsverk: Int,
        muligeDagsverk: Int,
        antallPersoner: Int
    ) {
        val parametre = parametreForStatistikk(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
        parametre.addValue("naring_kode", næringskode5Siffer.femsifferIdentifikator)
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring5siffer "
                    + "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, "
                    + "mulige_dagsverk) "
                    + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, "
                    + ":tapte_dagsverk, :mulige_dagsverk)",
            parametre
        )
    }


    fun opprettStatistikkForNæringer(
        sykefraværStatistikkNæringRepository: SykefraværStatistikkNæringRepository
    ) {
        sykefraværStatistikkNæringRepository.settInn(
            listOf(
                SykefraværsstatistikkForNæring(
                    årstall = SISTE_PUBLISERTE_KVARTAL.årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.kvartal,
                    næringkode = "10",
                    antallPersoner = 50,
                    tapteDagsverk = BigDecimal("20000.0"),
                    muligeDagsverk = BigDecimal("1000000.0")
                ),
                SykefraværsstatistikkForNæring(
                    årstall = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).kvartal,
                    næringkode = "10",
                    antallPersoner = 50,
                    tapteDagsverk = BigDecimal("30000.0"),
                    muligeDagsverk = BigDecimal("1000000.0")
                ),
                SykefraværsstatistikkForNæring(
                    årstall = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).kvartal,
                    næringkode = "10",
                    antallPersoner = 50,
                    tapteDagsverk = BigDecimal("40000.0"),
                    muligeDagsverk = BigDecimal("1000000.0")
                ),
                SykefraværsstatistikkForNæring(
                    årstall = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3).årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3).kvartal,
                    næringkode = "10",
                    antallPersoner = 50,
                    tapteDagsverk = BigDecimal("50000.0"),
                    muligeDagsverk = BigDecimal("1000000.0")
                ),
                SykefraværsstatistikkForNæring(
                    årstall = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(4).årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(4).kvartal,
                    næringkode = "10",
                    antallPersoner = 50,
                    tapteDagsverk = BigDecimal("60000.0"),
                    muligeDagsverk = BigDecimal("1000000.0")
                ),
                SykefraværsstatistikkForNæring(
                    årstall = SISTE_PUBLISERTE_KVARTAL.årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.kvartal,
                    næringkode = "88",
                    antallPersoner = 50,
                    tapteDagsverk = BigDecimal("25000.0"),
                    muligeDagsverk = BigDecimal("1000000.0")
                ),
                SykefraværsstatistikkForNæring(
                    årstall = SISTE_PUBLISERTE_KVARTAL.minusEttÅr().årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.minusEttÅr().kvartal,
                    næringkode = "88",
                    antallPersoner = 50,
                    tapteDagsverk = BigDecimal("30000.0"),
                    muligeDagsverk = BigDecimal("1000000.0")
                ),
            )
        )
    }

    fun ImporttidspunktRepository.slettAlleImporttidspunkt() {
        transaction { deleteAll() }
    }


    fun hentAlleKafkaUtsendingHistorikkData(
        jdbcTemplate: NamedParameterJdbcTemplate
    ): List<KafkaUtsendingHistorikkData> {
        return jdbcTemplate.query(
            "select orgnr, key_json, value_json, opprettet " + "from kafka_utsending_historikk ",
            MapSqlParameterSource()
        ) { resultSet: ResultSet, rowNum: Int ->
            KafkaUtsendingHistorikkData(
                resultSet.getString("orgnr"),
                resultSet.getString("key_json"),
                resultSet.getString("value_json"),
                resultSet.getTimestamp("opprettet").toLocalDateTime()
            )
        }
    }


    fun opprettUtsendingHistorikk(
        jdbcTemplate: NamedParameterJdbcTemplate,
        kafkaUtsendingHistorikkData: KafkaUtsendingHistorikkData
    ) {
        val parametre = MapSqlParameterSource()
        parametre.addValue("orgnr", kafkaUtsendingHistorikkData.orgnr)
        parametre.addValue("key", kafkaUtsendingHistorikkData.key)
        parametre.addValue("value", kafkaUtsendingHistorikkData.value)
        jdbcTemplate.update(
            "insert into kafka_utsending_historikk (orgnr, key_json, value_json) "
                    + "VALUES (:orgnr, :key, :value)",
            parametre
        )
    }

    private fun SykefravarStatistikkVirksomhetGraderingRepository.slettAlt() {
        transaction { deleteAll() }
    }
}
