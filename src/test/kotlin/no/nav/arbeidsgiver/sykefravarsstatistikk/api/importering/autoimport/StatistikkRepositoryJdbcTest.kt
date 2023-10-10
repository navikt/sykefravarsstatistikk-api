package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AssertUtils.assertBigDecimalIsEqual
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.NÆRINGSKODE_2SIFFER
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.NÆRINGSKODE_5SIFFER
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.ORGNR_VIRKSOMHET_1
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.parametreForStatistikk
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Varighetskategori.Companion.fraKode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.StatistikkRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.math.BigDecimal
import java.sql.ResultSet

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class StatistikkRepositoryJdbcTest {
    @Autowired
    private val jdbcTemplate: NamedParameterJdbcTemplate? = null
    private var statistikkRepository: StatistikkRepository? = null
    @BeforeEach
    fun setUp() {
        statistikkRepository = StatistikkRepository(jdbcTemplate!!)
        slettAllStatistikkFraDatabase(jdbcTemplate)
    }

    @AfterEach
    fun tearDown() {
        slettAllStatistikkFraDatabase(jdbcTemplate!!)
    }

    @Test
    fun hentSisteÅrstallOgKvartalForSykefraværsstatistikk__skal_returnere_siste_ÅrstallOgKvartal_for_import() {
        jdbcTemplate!!.update(
            "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametreForStatistikk(2019, 2, 10, 4, 100)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_land (arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametreForStatistikk(2019, 1, 10, 5, 100)
        )
        val årstallOgKvartal =
            statistikkRepository!!.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(Statistikkilde.LAND)
        Assertions.assertThat(årstallOgKvartal).isEqualTo(ÅrstallOgKvartal(2019, 2))
    }

    @Test
    fun batchOpprettSykefraværsstatistikkNæringMedVarighet__skal_lagre_data_i_tabellen() {
        val list: MutableList<SykefraværsstatistikkNæringMedVarighet> = ArrayList()
        val statistikkMedVarighet = SykefraværsstatistikkNæringMedVarighet(
            2019, 1, "03123", "A", 14, BigDecimal("55.123"), BigDecimal("856.891")
        )
        list.add(statistikkMedVarighet)
        statistikkRepository!!.batchOpprettSykefraværsstatistikkNæringMedVarighet(
            list, statistikkRepository!!.INSERT_BATCH_STØRRELSE
        )
        val resultList = hentSykefraværprosentNæringMedVarighet()
        Assertions.assertThat(resultList.size).isEqualTo(1)
        Assertions.assertThat(resultList[0])
            .isEqualTo(
                UmaskertSykefraværForEttKvartalMedVarighet(
                    ÅrstallOgKvartal(2019, 1),
                    BigDecimal("55.123"),
                    BigDecimal("856.891"),
                    14,
                    Varighetskategori._1_DAG_TIL_7_DAGER
                )
            )
    }

    @Test
    fun batchOpprettSykefraværsstatistikkVirksomhetMedGradering__skal_lagre_data_i_tabellen() {
        val list: MutableList<SykefraværsstatistikkVirksomhetMedGradering> = ArrayList()
        val gradertSykemelding = SykefraværsstatistikkVirksomhetMedGradering(
            2020,
            3,
            ORGNR_VIRKSOMHET_1,
            NÆRINGSKODE_2SIFFER,
            NÆRINGSKODE_5SIFFER,
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            1,
            BigDecimal(3).setScale(6),
            3,
            13,
            BigDecimal(16).setScale(6),
            BigDecimal(100).setScale(6)
        )
        list.add(gradertSykemelding)
        statistikkRepository!!.batchOpprettSykefraværsstatistikkVirksomhetMedGradering(
            list, statistikkRepository!!.INSERT_BATCH_STØRRELSE
        )
        val resultList = hentSykefraværprosentMedGradering()
        Assertions.assertThat(resultList.size).isEqualTo(1)
        Assertions.assertThat(resultList[0])
            .isEqualTo(
                UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(2020, 3), BigDecimal("3"), BigDecimal("100"), 13
                )
            )
    }

    @Test
    fun batchOpprettSykefraværsstatistikkVirksomhet__skal_lagre_data_i_tabellen_med_rectype() {
        val list: MutableList<SykefraværsstatistikkVirksomhet> = ArrayList()
        val sykefraværsstatistikkVirksomhet = SykefraværsstatistikkVirksomhet(
            2019,
            3,
            ORGNR_VIRKSOMHET_1,
            Varighetskategori._1_DAG_TIL_7_DAGER.kode,
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            1,
            BigDecimal(16).setScale(6),
            BigDecimal(100).setScale(6)
        )
        list.add(sykefraværsstatistikkVirksomhet)
        statistikkRepository!!.importSykefraværsstatistikkVirksomhet(list, ÅrstallOgKvartal(2019, 3))
        val resultList = hentRawDataStatistikkVirksomhet()
        Assertions.assertThat(resultList.size).isEqualTo(1)
        assertIsEquals(
            resultList[0],
            RawDataStatistikkVirksomhet(
                2019,
                3,
                ORGNR_VIRKSOMHET_1,
                Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
                BigDecimal("16"),
                BigDecimal("100"),
                1
            )
        )
    }

    @Test
    fun slettSykefraværsstatistikkNæringMedVarighet__skal_slette_data_i_tabellen() {
        lagreSykefraværprosentNæringMedVarighet("01", "A", 2018, 3)
        lagreSykefraværprosentNæringMedVarighet("02", "A", 2018, 3)
        lagreSykefraværprosentNæringMedVarighet("01", "A", 2018, 4)
        lagreSykefraværprosentNæringMedVarighet("02", "A", 2018, 4)
        lagreSykefraværprosentNæringMedVarighet("01", "A", 2019, 1)
        lagreSykefraværprosentNæringMedVarighet("02", "A", 2019, 1)
        val antallSlettet = statistikkRepository!!.slettSykefraværsstatistikkNæringMedVarighet(
            ÅrstallOgKvartal(2019, 1)
        )
        val list = hentSykefraværprosentNæringMedVarighet()
        Assertions.assertThat(list.size).isEqualTo(4)
        Assertions.assertThat(antallSlettet).isEqualTo(2)
    }

    private fun lagreSykefraværprosentNæringMedVarighet(
        næringkode: String, varighet: String, årstall: Int, kvartal: Int
    ) {
        jdbcTemplate!!.update(
            String.format(
                "insert into sykefravar_statistikk_naring_med_varighet "
                        + "(arstall, kvartal, naring_kode, varighet, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "values (%d, %d, '%s', '%s', 15, 30, 300)",
                årstall, kvartal, næringkode, varighet
            ),
            MapSqlParameterSource()
        )
    }

    private fun hentSykefraværprosentNæringMedVarighet(): List<UmaskertSykefraværForEttKvartalMedVarighet> {
        return jdbcTemplate!!.query(
            "select * from sykefravar_statistikk_naring_med_varighet",
            MapSqlParameterSource()
        ) { rs: ResultSet, rowNum: Int ->
            UmaskertSykefraværForEttKvartalMedVarighet(
                ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk"),
                rs.getInt("antall_personer"),
                fraKode(rs.getString("varighet"))
            )
        }
    }

    private fun hentSykefraværprosentMedGradering(): List<UmaskertSykefraværForEttKvartal> {
        return jdbcTemplate!!.query(
            "select * from sykefravar_statistikk_virksomhet_med_gradering",
            MapSqlParameterSource()
        ) { rs: ResultSet, rowNum: Int ->
            UmaskertSykefraværForEttKvartal(
                ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
                rs.getBigDecimal("tapte_dagsverk_gradert_sykemelding"),
                rs.getBigDecimal("mulige_dagsverk"),
                rs.getInt("antall_personer")
            )
        }
    }

    private fun hentRawDataStatistikkVirksomhet(): List<RawDataStatistikkVirksomhet> {
        return jdbcTemplate!!.query(
            "select * from sykefravar_statistikk_virksomhet",
            MapSqlParameterSource()
        ) { rs: ResultSet, rowNum: Int ->
            RawDataStatistikkVirksomhet(
                rs.getInt("arstall"),
                rs.getInt("kvartal"),
                rs.getString("orgnr"),
                rs.getString("varighet"),
                rs.getString("rectype"),
                rs.getBigDecimal("tapte_dagsverk"),
                rs.getBigDecimal("mulige_dagsverk"),
                rs.getInt("antall_personer")
            )
        }
    }

    inner class RawDataStatistikkVirksomhet(
        var årstall: Int,
        var kvartal: Int,
        var orgnr: String,
        var varighet: String?,
        var rectype: String,
        var tapteDagsverk: BigDecimal,
        var muligeDagsverk: BigDecimal,
        var antallPersoner: Int
    )

    companion object {
        fun assertIsEquals(
            actual: RawDataStatistikkVirksomhet, expected: RawDataStatistikkVirksomhet
        ) {
            Assertions.assertThat(actual.årstall).isEqualTo(expected.årstall)
            Assertions.assertThat(actual.kvartal).isEqualTo(expected.kvartal)
            Assertions.assertThat(actual.antallPersoner).isEqualTo(expected.antallPersoner)
            Assertions.assertThat(actual.varighet).isEqualTo(expected.varighet)
            Assertions.assertThat(actual.rectype).isEqualTo(expected.rectype)
            assertBigDecimalIsEqual(actual.muligeDagsverk, expected.muligeDagsverk)
            assertBigDecimalIsEqual(actual.tapteDagsverk, expected.tapteDagsverk)
        }
    }
}
