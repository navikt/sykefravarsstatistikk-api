package infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.UmaskertSykefraværForEttKvartalMedVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
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
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import testUtils.AssertUtils.assertBigDecimalIsEqual
import testUtils.TestData.NÆRINGSKODE_2SIFFER
import testUtils.TestData.NÆRINGSKODE_5SIFFER
import testUtils.TestData.ORGNR_VIRKSOMHET_1
import testUtils.TestUtils.slettAllStatistikkFraDatabase
import java.math.BigDecimal

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class StatistikkRepositoryJdbcTest {

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

        sykefraværStatistikkNæringMedVarighetRepository.settInn(
            listOf(
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2019,
                    kvartal = 1,
                    næringkode = "03123",
                    varighet = "A",
                    antallPersoner = 14,
                    tapteDagsverk = BigDecimal("55.123"),
                    muligeDagsverk = BigDecimal("856.891")
                )
            )
        )
        val resultat = sykefraværStatistikkNæringMedVarighetRepository.hentAlt()

        resultat.size shouldBe 1
        resultat[0] shouldBeEqual
                UmaskertSykefraværForEttKvartalMedVarighet(
                    ÅrstallOgKvartal(2019, 1),
                    BigDecimal("55.123"),
                    BigDecimal("856.891"),
                    14,
                    Varighetskategori._1_DAG_TIL_7_DAGER
                )
    }

    @Test
    fun batchOpprettSykefraværsstatistikkVirksomhetMedGradering__skal_lagre_data_i_tabellen() {
        val list: MutableList<SykefraværsstatistikkVirksomhetMedGradering> = ArrayList()
        val gradertSykemelding = SykefraværsstatistikkVirksomhetMedGradering(
            årstall = 2020,
            kvartal = 3,
            orgnr = ORGNR_VIRKSOMHET_1,
            næring = NÆRINGSKODE_2SIFFER,
            næringkode = NÆRINGSKODE_5SIFFER,
            rectype = DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            antallGraderteSykemeldinger = 1,
            tapteDagsverkGradertSykemelding = BigDecimal(3).setScale(6),
            antallSykemeldinger = 3,
            antallPersoner = 13,
            tapteDagsverk = BigDecimal(16).setScale(6),
            muligeDagsverk = BigDecimal(100).setScale(6)
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
        sykefraværStatistikkNæringMedVarighetRepository.settInn(
            listOf(
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2018,
                    kvartal = 3,
                    tapteDagsverk = 30.toBigDecimal(),
                    muligeDagsverk = 300.toBigDecimal(),
                    antallPersoner = 15,
                    varighet = "A",
                    næringkode = "01000"
                ),
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2018,
                    kvartal = 3,
                    tapteDagsverk = 30.toBigDecimal(),
                    muligeDagsverk = 300.toBigDecimal(),
                    antallPersoner = 15,
                    varighet = "A",
                    næringkode = "02000"
                ),
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2018,
                    kvartal = 4,
                    tapteDagsverk = 30.toBigDecimal(),
                    muligeDagsverk = 300.toBigDecimal(),
                    antallPersoner = 15,
                    varighet = "A",
                    næringkode = "01000"
                ),
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2018,
                    kvartal = 4,
                    tapteDagsverk = 30.toBigDecimal(),
                    muligeDagsverk = 300.toBigDecimal(),
                    antallPersoner = 15,
                    varighet = "A",
                    næringkode = "02000"
                ),
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2019,
                    kvartal = 1,
                    tapteDagsverk = 30.toBigDecimal(),
                    muligeDagsverk = 300.toBigDecimal(),
                    antallPersoner = 15,
                    varighet = "A",
                    næringkode = "01000"
                ),
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2019,
                    kvartal = 1,
                    tapteDagsverk = 30.toBigDecimal(),
                    muligeDagsverk = 300.toBigDecimal(),
                    antallPersoner = 15,
                    varighet = "A",
                    næringkode = "02000"
                )
            )
        )

        val antallSlettet = sykefraværStatistikkNæringMedVarighetRepository.slettKvartal(ÅrstallOgKvartal(2019, 1))

        val list = sykefraværStatistikkNæringMedVarighetRepository.hentAlt()

        list.size shouldBe 4
        antallSlettet shouldBe 2
    }

    private fun SykefraværStatistikkNæringMedVarighetRepository.hentAlt(): List<UmaskertSykefraværForEttKvartalMedVarighet> {
        return transaction {
            selectAll().map {
                UmaskertSykefraværForEttKvartalMedVarighet(
                    årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal]),
                    tapteDagsverk = it[tapteDagsverk].toBigDecimal(),
                    muligeDagsverk = it[muligeDagsverk].toBigDecimal(),
                    antallPersoner = it[antallPersoner],
                    varighet = Varighetskategori.fraKode(it[varighet].toString())
                )
            }
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
