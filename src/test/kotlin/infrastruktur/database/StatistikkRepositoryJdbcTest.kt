package infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.UmaskertSykefraværForEttKvartalMedVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori.Companion.fraKode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
import org.assertj.core.api.Assertions
import org.jetbrains.exposed.sql.selectAll
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
import testUtils.AssertUtils.assertBigDecimalIsEqual
import testUtils.TestData.NÆRINGSKODE_2SIFFER
import testUtils.TestData.NÆRINGSKODE_5SIFFER
import testUtils.TestData.ORGNR_VIRKSOMHET_1
import testUtils.TestUtils.slettAllStatistikkFraDatabase
import java.math.BigDecimal
import java.sql.ResultSet

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class StatistikkRepositoryJdbcTest {
    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var statistikkRepository: StatistikkRepository

    @Autowired
    private lateinit var sykefravarStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository

    @Autowired
    private lateinit var sykefravarStatistikkVirksomhetGraderingRepository: SykefravarStatistikkVirksomhetGraderingRepository

    @Autowired
    private lateinit var sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository

    @Autowired
    private lateinit var sykefraværStatistikkNæringMedVarighetRepository: SykefraværStatistikkNæringMedVarighetRepository

    @BeforeEach
    fun setUp() {
        slettAllStatistikkFraDatabase(
            sykefravarStatistikkVirksomhetRepository = sykefravarStatistikkVirksomhetRepository,
            sykefraværStatistikkLandRepository = sykefraværStatistikkLandRepository,
            sykefraværStatistikkNæringMedVarighetRepository = sykefraværStatistikkNæringMedVarighetRepository
        )
    }

    @AfterEach
    fun tearDown() {
        slettAllStatistikkFraDatabase(
            sykefravarStatistikkVirksomhetRepository = sykefravarStatistikkVirksomhetRepository,
            sykefraværStatistikkLandRepository = sykefraværStatistikkLandRepository
        )
    }

    @Test
    fun hentSisteÅrstallOgKvartalForSykefraværsstatistikk__skal_returnere_siste_ÅrstallOgKvartal_for_import() {

        sykefraværStatistikkLandRepository.settInn(
            listOf(
                SykefraværsstatistikkLand(
                    årstall = 2019,
                    kvartal = 2,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(4),
                    muligeDagsverk = BigDecimal(100),
                ),
                SykefraværsstatistikkLand(
                    årstall = 2019,
                    kvartal = 1,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(5),
                    muligeDagsverk = BigDecimal(100)
                )
            )
        )

        val årstallOgKvartal = sykefraværStatistikkLandRepository.hentNyesteKvartal()
        Assertions.assertThat(årstallOgKvartal).isEqualTo(ÅrstallOgKvartal(2019, 2))
    }

    @Test
    fun batchOpprettSykefraværsstatistikkNæringMedVarighet__skal_lagre_data_i_tabellen() {
        val list: MutableList<SykefraværsstatistikkNæringMedVarighet> = ArrayList()
        val statistikkMedVarighet = SykefraværsstatistikkNæringMedVarighet(
            2019, 1, "03123", "A", 14, BigDecimal("55.123"), BigDecimal("856.891")
        )
        list.add(statistikkMedVarighet)
        statistikkRepository.batchOpprettSykefraværsstatistikkNæringMedVarighet(
            list, statistikkRepository.INSERT_BATCH_STØRRELSE
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
        sykefravarStatistikkVirksomhetGraderingRepository.opprettSykefraværsstatistikkVirksomhetMedGradering(list)
        val resultList = sykefravarStatistikkVirksomhetGraderingRepository.hentSykefraværprosentMedGradering()
        Assertions.assertThat(resultList.size).isEqualTo(1)
        Assertions.assertThat(resultList[0])
            .isEqualTo(
                UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(2020, 3), BigDecimal("3"), BigDecimal("100"), 13
                )
            )
    }

    @Test
    fun `settInn skal lagre riktige data i tabellen`() {
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

        sykefravarStatistikkVirksomhetRepository.settInn(list)

        val statistikkIDatabasen = sykefravarStatistikkVirksomhetRepository.hentAlt()

        Assertions.assertThat(statistikkIDatabasen.size).isEqualTo(1)
        assertEquals(
            statistikkIDatabasen.first(),
            RawDataStatistikkVirksomhet(
                årstall = 2019,
                kvartal = 3,
                orgnr = ORGNR_VIRKSOMHET_1,
                varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                rectype = DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
                tapteDagsverk = BigDecimal("16"),
                muligeDagsverk = BigDecimal("100"),
                antallPersoner = 1
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
        val antallSlettet = statistikkRepository.slettSykefraværsstatistikkNæringMedVarighet(
            ÅrstallOgKvartal(2019, 1)
        )
        val list = hentSykefraværprosentNæringMedVarighet()
        Assertions.assertThat(list.size).isEqualTo(4)
        Assertions.assertThat(antallSlettet).isEqualTo(2)
    }

    private fun lagreSykefraværprosentNæringMedVarighet(
        næringkode: String, varighet: String, årstall: Int, kvartal: Int
    ) {
        jdbcTemplate.update(
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
        return jdbcTemplate.query(
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

    private fun SykefravarStatistikkVirksomhetGraderingRepository.hentSykefraværprosentMedGradering(): List<UmaskertSykefraværForEttKvartal> {
        return transaction {
            selectAll()
                .map {
                    UmaskertSykefraværForEttKvartal(
                        årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal]),
                        dagsverkTeller = it[tapteDagsverkGradertSykemelding].toBigDecimal(),
                        dagsverkNevner = it[muligeDagsverk].toBigDecimal(),
                        antallPersoner = it[antallPersoner]
                    )
                }
        }
    }

    private fun SykefravarStatistikkVirksomhetRepository.hentAlt(): List<RawDataStatistikkVirksomhet> {
        return transaction {
            selectAll().map {
                RawDataStatistikkVirksomhet(
                    årstall = it[årstall],
                    kvartal = it[kvartal],
                    orgnr = it[orgnr],
                    varighet = it[varighet].toString(),
                    rectype = it[virksomhetstype].toString(),
                    tapteDagsverk = it[tapteDagsverk].toBigDecimal(),
                    muligeDagsverk = it[muligeDagsverk].toBigDecimal(),
                    antallPersoner = it[antallPersoner],
                )
            }
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
        fun assertEquals(
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
