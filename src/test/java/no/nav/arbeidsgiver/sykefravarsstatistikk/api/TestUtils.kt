package no.nav.arbeidsgiver.sykefravarsstatistikk.api

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværsstatistikkSektorUtils
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import java.math.BigDecimal
import java.sql.ResultSet
import java.sql.Timestamp

object TestUtils {
    @JvmField
    val PRODUKSJON_NYTELSESMIDLER = Næring("10")
    @JvmField
    val SISTE_PUBLISERTE_KVARTAL = ÅrstallOgKvartal(2022, 1)
    fun sisteKvartalMinus(n: Int): ÅrstallOgKvartal {
        return SISTE_PUBLISERTE_KVARTAL.minusKvartaler(n)
    }

    @JvmStatic
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

    @JvmStatic
    fun slettAllStatistikkFraDatabase(jdbcTemplate: NamedParameterJdbcTemplate) {
        jdbcTemplate.update(
            "delete from sykefravar_statistikk_virksomhet", MapSqlParameterSource()
        )
        jdbcTemplate.update("delete from sykefravar_statistikk_naring", MapSqlParameterSource())
        jdbcTemplate.update(
            "delete from sykefravar_statistikk_naring_med_varighet", MapSqlParameterSource()
        )
        jdbcTemplate.update(
            "delete from sykefravar_statistikk_virksomhet_med_gradering", MapSqlParameterSource()
        )
        jdbcTemplate.update(
            "delete from sykefravar_statistikk_naring5siffer", MapSqlParameterSource()
        )
        jdbcTemplate.update("delete from sykefravar_statistikk_sektor", MapSqlParameterSource())
        jdbcTemplate.update("delete from sykefravar_statistikk_land", MapSqlParameterSource())
    }

    @JvmStatic
    fun slettAllEksportDataFraDatabase(jdbcTemplate: NamedParameterJdbcTemplate) {
        jdbcTemplate.update("delete from virksomhet_metadata", MapSqlParameterSource())
        jdbcTemplate.update("delete from eksport_per_kvartal", MapSqlParameterSource())
        jdbcTemplate.update("delete from kafka_utsending_historikk", MapSqlParameterSource())
        jdbcTemplate.update(
            "delete from virksomheter_bekreftet_eksportert", MapSqlParameterSource()
        )
    }

    @JvmStatic
    fun opprettTestVirksomhetMetaData(
        jdbcTemplate: NamedParameterJdbcTemplate, årstall: Int, kvartal: Int, orgnr: String?
    ) {
        opprettTestVirksomhetMetaData(jdbcTemplate, årstall, kvartal, orgnr, false)
    }

    @JvmStatic
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

    @JvmStatic
    fun opprettStatistikkForLand(jdbcTemplate: NamedParameterJdbcTemplate) {
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, "
                    + "tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, "
                    + ":mulige_dagsverk)",
            parametreForStatistikk(
                SISTE_PUBLISERTE_KVARTAL.årstall,
                SISTE_PUBLISERTE_KVARTAL.kvartal,
                10,
                4,
                100
            )
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, "
                    + "tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, "
                    + ":mulige_dagsverk)",
            parametreForStatistikk(
                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).årstall,
                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).kvartal,
                10,
                5,
                100
            )
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, "
                    + "tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, "
                    + ":mulige_dagsverk)",
            parametreForStatistikk(
                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).årstall,
                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).kvartal,
                10,
                6,
                100
            )
        )
    }

    @JvmStatic
    fun opprettStatistikkForSektor(jdbcTemplate: NamedParameterJdbcTemplate?) {
        SykefraværsstatistikkSektorUtils(jdbcTemplate)
            .getBatchCreateFunction(
                java.util.List.of(
                    SykefraværsstatistikkSektor(
                        SISTE_PUBLISERTE_KVARTAL.årstall,
                        SISTE_PUBLISERTE_KVARTAL.kvartal,
                        "1",
                        10,
                        BigDecimal("657853.346702"),
                        BigDecimal("13558710.866603")
                    )
                )
            )
            .apply()
    }

    @JvmStatic
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

    @JvmStatic
    fun opprettStatistikkForNæringer(jdbcTemplate: NamedParameterJdbcTemplate) {
        opprettStatistikkForNæring(
            jdbcTemplate,
            Næring("10"),
            SISTE_PUBLISERTE_KVARTAL.årstall,
            SISTE_PUBLISERTE_KVARTAL.kvartal,
            20000,
            1000000,
            50
        )
        opprettStatistikkForNæring(
            jdbcTemplate,
            Næring("10"),
            SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).årstall,
            SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).kvartal,
            30000,
            1000000,
            50
        )
        opprettStatistikkForNæring(
            jdbcTemplate,
            Næring("10"),
            SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).årstall,
            SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).kvartal,
            40000,
            1000000,
            50
        )
        opprettStatistikkForNæring(
            jdbcTemplate,
            Næring("10"),
            SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3).årstall,
            SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3).kvartal,
            50000,
            1000000,
            50
        )
        opprettStatistikkForNæring(
            jdbcTemplate,
            Næring("10"),
            SISTE_PUBLISERTE_KVARTAL.minusKvartaler(4).årstall,
            SISTE_PUBLISERTE_KVARTAL.minusKvartaler(4).kvartal,
            60000,
            1000000,
            50
        )
        opprettStatistikkForNæring(
            jdbcTemplate,
            Næring("88"),
            SISTE_PUBLISERTE_KVARTAL.årstall,
            SISTE_PUBLISERTE_KVARTAL.kvartal,
            25000,
            1000000,
            50
        )
        opprettStatistikkForNæring(
            jdbcTemplate,
            Næring("88"),
            SISTE_PUBLISERTE_KVARTAL.minusEttÅr().årstall,
            SISTE_PUBLISERTE_KVARTAL.minusEttÅr().kvartal,
            30000,
            1000000,
            50
        )
    }

    @JvmStatic
    fun opprettStatistikkForNæring(
        jdbcTemplate: NamedParameterJdbcTemplate,
        næring: Næring,
        årstall: Int,
        kvartal: Int,
        tapteDagsverk: Int,
        muligeDagsverk: Int,
        antallPersoner: Int
    ) {
        val parametre = parametreForStatistikk(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
        parametre.addValue("naring_kode", næring.tosifferIdentifikator)
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring "
                    + "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, "
                    + "mulige_dagsverk) "
                    + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, "
                    + ":tapte_dagsverk, :mulige_dagsverk)",
            parametre
        )
    }

    @JvmStatic
    fun opprettStatistikkForVirksomhet(
        jdbcTemplate: NamedParameterJdbcTemplate,
        orgnr: String?,
        årstall: Int,
        kvartal: Int,
        tapteDagsverk: Int,
        muligeDagsverk: Int,
        antallPersoner: Int
    ) {
        val parametre = parametreForStatistikk(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
        parametre.addValue("orgnr", orgnr)
        parametre.addValue("varighet", "A")
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_virksomhet (arstall, kvartal, orgnr, varighet,"
                    + " antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:arstall, :kvartal, :orgnr, :varighet, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre
        )
    }

    @JvmStatic
    fun slettAlleImporttidspunkt(jdbcTemplate: NamedParameterJdbcTemplate) {
        jdbcTemplate.update("delete from importtidspunkt", MapSqlParameterSource())
    }

    @JvmStatic
    fun skrivSisteImporttidspunktTilDb(jdbcTemplate: NamedParameterJdbcTemplate) {
        skrivImporttidspunktTilDb(
            jdbcTemplate,
            ImporttidspunktDto(
                Timestamp.valueOf("2022-06-02 10:00:00.0"), SISTE_PUBLISERTE_KVARTAL
            )
        )
    }

    @JvmStatic
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

    @JvmStatic
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

    private fun skrivImporttidspunktTilDb(
        jdbcTemplate: NamedParameterJdbcTemplate, importtidspunkt: ImporttidspunktDto
    ) {
        jdbcTemplate.update(
            "insert into importtidspunkt (aarstall, kvartal, importert) values "
                    + "(:aarstall, :kvartal, :importert)",
            MapSqlParameterSource()
                .addValue("aarstall", importtidspunkt.gjeldendePeriode.årstall)
                .addValue("kvartal", importtidspunkt.gjeldendePeriode.kvartal)
                .addValue("importert", importtidspunkt.importertDato)
        )
    }
}