package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.Orgenhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.StatistikkildeDvh
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.DatavarehusRepositoryJdbcTestUtils.cleanUpTestDb
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.DatavarehusRepositoryJdbcTestUtils.insertSykefraværsstatistikkNærin5SiffergInDvhTabell
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.DatavarehusRepositoryJdbcTestUtils.insertSykefraværsstatistikkNæringInDvhTabell
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.DatavarehusRepositoryJdbcTestUtils.insertSykefraværsstatistikkVirksomhetInDvhTabell
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.*
import org.assertj.core.api.AssertionsForClassTypes
import org.jetbrains.exposed.sql.insert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import testUtils.TestData.NÆRINGSKODE_2SIFFER
import testUtils.TestData.NÆRINGSKODE_5SIFFER
import testUtils.TestData.ORGNR_VIRKSOMHET_1
import testUtils.TestData.ORGNR_VIRKSOMHET_2
import testUtils.TestData.ORGNR_VIRKSOMHET_3
import java.math.BigDecimal

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class DatavarehusRepositoryJdbcTest {
    @Qualifier("datavarehusJdbcTemplate")
    @Autowired
    lateinit var namedParameterJdbcTemplate: NamedParameterJdbcTemplate

    @Autowired
    lateinit var datavarehusLandRespository: DatavarehusLandRespository

    @Autowired
    lateinit var datavarehusNæringRepository: DatavarehusNæringRepository

    @Autowired
    private lateinit var datavarehusNæringskodeRepository: DatavarehusNæringskodeRepository

    @Autowired
    private lateinit var datavarehusAggregertRepositoryV2: DatavarehusAggregertRepositoryV2

    @Autowired
    private lateinit var repository: DatavarehusRepository

    @BeforeEach
    fun setUp() {
        cleanUpTestDb(
            jdbcTemplate = namedParameterJdbcTemplate,
            datavarehusLandRespository = datavarehusLandRespository,
            datavarehusAggregertRepositoryV2 = datavarehusAggregertRepositoryV2
        )
    }

    @Test
    fun hentSisteÅrstallOgKvartalFraSykefraværsstatistikk__returnerer_siste_ÅrstallOgKvartal_for_Land_og_sektor() {
        datavarehusLandRespository.settInn(
            2019,
            4,
            4,
            5,
            100
        )
        datavarehusLandRespository.settInn(
            2019,
            4,
            6,
            10,
            100
        )
        datavarehusLandRespository.settInn(
            2020,
            1,
            1,
            1,
            10
        )
        val sisteÅrstallOgKvartal = datavarehusLandRespository.hentSisteKvartal()
        AssertionsForClassTypes.assertThat(sisteÅrstallOgKvartal).isEqualTo(ÅrstallOgKvartal(2020, 1))
    }

    @Test
    fun hentSisteÅrstallOgKvartalFraSykefraværsstatistikk__returnerer_siste_ÅrstallOgKvartal_for_Næring() {
        insertSykefraværsstatistikkNæringInDvhTabell(
            namedParameterJdbcTemplate, 2019, 4, 4, "23", "K", 5, 100
        )
        insertSykefraværsstatistikkNæringInDvhTabell(
            namedParameterJdbcTemplate, 2022, 3, 2, "90", "M", 12, 100
        )
        val sisteÅrstallOgKvartal = datavarehusNæringRepository.hentSisteKvartal()
        AssertionsForClassTypes.assertThat(sisteÅrstallOgKvartal).isEqualTo(ÅrstallOgKvartal(2022, 3))
    }

    @Test
    fun hentSisteÅrstallOgKvartalFraSykefraværsstatistikk__returnerer_siste_ÅrstallOgKvartal_for_Næring5Siffer() {
        insertSykefraværsstatistikkNærin5SiffergInDvhTabell(
            namedParameterJdbcTemplate, 2022, 3, 4, "01110", "K", 5, 100
        )
        insertSykefraværsstatistikkNærin5SiffergInDvhTabell(
            namedParameterJdbcTemplate, 2020, 1, 2, "01110", "M", 12, 100
        )
        val sisteÅrstallOgKvartal = datavarehusNæringskodeRepository.hentSisteKvartal()
        AssertionsForClassTypes.assertThat(sisteÅrstallOgKvartal).isEqualTo(ÅrstallOgKvartal(2022, 3))
    }

    @Test
    fun hentSisteÅrstallOgKvartalFraSykefraværsstatistikk__returnerer_siste_ÅrstallOgKvartal_for_Virksomhet() {
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            namedParameterJdbcTemplate,
            2018,
            4,
            4,
            ORGNR_VIRKSOMHET_1,
            "10062", Varighetskategori._8_DAGER_TIL_16_DAGER,
            "K",
            5,
            100
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            namedParameterJdbcTemplate,
            2019,
            1,
            5,
            ORGNR_VIRKSOMHET_2,
            "10062", Varighetskategori._8_DAGER_TIL_16_DAGER,
            "M",
            5,
            101
        )
        val sisteÅrstallOgKvartal =
            repository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(StatistikkildeDvh.VIRKSOMHET)
        AssertionsForClassTypes.assertThat(sisteÅrstallOgKvartal).isEqualTo(ÅrstallOgKvartal(2019, 1))
    }

    @Test
    fun hentSykefraværsstatistikkSektor__lager_sum_og_returnerer_antall_tapte_og_mulige_dagsverk() {
        datavarehusLandRespository.settInn(
            2018,
            4,
            1,
            5,
            100
        )
        datavarehusLandRespository.settInn(
            2018,
            4,
            3,
            10,
            100
        )
        datavarehusLandRespository.settInn(
            2019,
            1,
            1,
            1,
            10
        )
        val sykefraværsstatistikkSektor = datavarehusLandRespository.hentSykefraværsstatistikkSektor(
            ÅrstallOgKvartal(2018, 4),
        )
        AssertionsForClassTypes.assertThat(sykefraværsstatistikkSektor.size).isEqualTo(1)
        val sykefraværsstatistikkSektorExpected =
            SykefraværsstatistikkSektor(2018, 4, "1", 4, BigDecimal("15.0"), BigDecimal("200.0"))
        val sykefraværsstatistikkSektorActual = sykefraværsstatistikkSektor[0]
        Assertions.assertTrue(
            ReflectionEquals(sykefraværsstatistikkSektorExpected)
                .matches(sykefraværsstatistikkSektorActual)
        )
    }

    @Test
    fun hentSykefraværsstatistikkLand__lager_sum_og_returnerer_antall_tapte_og_mulige_dagsverk() {
        datavarehusLandRespository.settInn(
            2018,
            4,
            4,
            5,
            100
        )
        datavarehusLandRespository.settInn(
            2018,
            4,
            6,
            10,
            100
        )
        datavarehusLandRespository.settInn(
            2019,
            1,
            1,
            1,
            10
        )
        val sykefraværsstatistikkLand = datavarehusLandRespository.hentFor(ÅrstallOgKvartal(2018, 4))
        AssertionsForClassTypes.assertThat(sykefraværsstatistikkLand.size).isEqualTo(1)
        AssertionsForClassTypes.assertThat(sykefraværsstatistikkLand[0])
            .isEqualTo(
                SykefraværsstatistikkLand(2018, 4, 10, BigDecimal("15.0"), BigDecimal("200.0"))
            )
    }

    @Test
    fun hentSykefraværsstatistikkVirksomhet__lager_sum_og_returnerer_antall_tapte_og_mulige_dagsverk() {
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            namedParameterJdbcTemplate,
            2018,
            4,
            4,
            ORGNR_VIRKSOMHET_1,
            "10062", Varighetskategori._1_DAG_TIL_7_DAGER,
            "K",
            5,
            100
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            namedParameterJdbcTemplate,
            2018,
            4,
            3,
            ORGNR_VIRKSOMHET_1,
            "10062", Varighetskategori._1_DAG_TIL_7_DAGER,
            "M",
            8,
            88
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            namedParameterJdbcTemplate,
            2018,
            4,
            6,
            ORGNR_VIRKSOMHET_2,
            "10062", Varighetskategori._1_DAG_TIL_7_DAGER,
            "K",
            3,
            75
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            namedParameterJdbcTemplate,
            2019,
            1,
            5,
            ORGNR_VIRKSOMHET_1,
            "10062", Varighetskategori._1_DAG_TIL_7_DAGER,
            "M",
            5,
            101
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            namedParameterJdbcTemplate,
            2019,
            2,
            9,
            ORGNR_VIRKSOMHET_1,
            "10062", Varighetskategori._8_DAGER_TIL_16_DAGER,
            "M",
            9,
            99
        )
        val sykefraværsstatistikkVirksomhet =
            repository.hentSykefraværsstatistikkVirksomhet(ÅrstallOgKvartal(2018, 4))
        AssertionsForClassTypes.assertThat(sykefraværsstatistikkVirksomhet.size).isEqualTo(2)
        val expected = SykefraværsstatistikkVirksomhet(
            2018,
            4,
            ORGNR_VIRKSOMHET_1,
            Varighetskategori._1_DAG_TIL_7_DAGER.kode,
            Rectype.VIRKSOMHET.kode,
            7,
            BigDecimal(13),
            BigDecimal(188)
        )
        AssertionsForClassTypes.assertThat(sykefraværsstatistikkVirksomhet[0]).isEqualTo(expected)
    }

    @Test
    fun hentSykefraværsstatistikkNæringMedVarighet__lager_sum_og_returnerer_antall_tapte_og_mulige_dagsverk_med_varighet() {
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            namedParameterJdbcTemplate,
            2018,
            4,
            4,
            ORGNR_VIRKSOMHET_1,
            "10062", Varighetskategori._1_DAG_TIL_7_DAGER,
            "K",
            5,
            100
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            namedParameterJdbcTemplate,
            2018,
            4,
            4,
            ORGNR_VIRKSOMHET_1,
            "10062", Varighetskategori._1_DAG_TIL_7_DAGER,
            "K",
            1,
            10,
            Rectype.FORETAK.kode
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            namedParameterJdbcTemplate,
            2018,
            4,
            4,
            ORGNR_VIRKSOMHET_1,
            "10062", Varighetskategori._8_DAGER_TIL_16_DAGER,
            "K",
            5,
            100
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            namedParameterJdbcTemplate,
            2018,
            4,
            3,
            ORGNR_VIRKSOMHET_1,
            "10062", Varighetskategori._1_DAG_TIL_7_DAGER,
            "M",
            8,
            88
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            namedParameterJdbcTemplate,
            2018,
            4,
            6,
            ORGNR_VIRKSOMHET_2,
            "10062", Varighetskategori._1_DAG_TIL_7_DAGER,
            "K",
            3,
            75
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            namedParameterJdbcTemplate,
            2018,
            4,
            6,
            ORGNR_VIRKSOMHET_3,
            "85000", Varighetskategori._1_DAG_TIL_7_DAGER,
            "K",
            10,
            80
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            namedParameterJdbcTemplate,
            2019,
            1,
            5,
            ORGNR_VIRKSOMHET_1,
            "10062", Varighetskategori._1_DAG_TIL_7_DAGER,
            "M",
            5,
            101
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            namedParameterJdbcTemplate,
            2019,
            2,
            9,
            ORGNR_VIRKSOMHET_3,
            "85000", Varighetskategori._8_DAGER_TIL_16_DAGER,
            "M",
            9,
            99
        )
        val sykefraværsstatistikkNæringMedVarighet =
            repository.hentSykefraværsstatistikkNæringMedVarighet(ÅrstallOgKvartal(2018, 4))
        AssertionsForClassTypes.assertThat(sykefraværsstatistikkNæringMedVarighet.size).isEqualTo(3)
        val expected = SykefraværsstatistikkNæringMedVarighet(
            2018,
            4,
            NÆRINGSKODE_5SIFFER,
            Varighetskategori._1_DAG_TIL_7_DAGER.kode,
            13,
            BigDecimal(16),
            BigDecimal(263)
        )
        AssertionsForClassTypes.assertThat(
            sykefraværsstatistikkNæringMedVarighet[0]
        ).isEqualTo(expected)
    }

    @Test
    fun hentSykefraværsstatistikkVirksomhetMedGradering__lager_sum_og_returnerer_antall_tapte_dagsverk_i_gradert_sykemelding_og_mulige_dagsverk() {
        datavarehusAggregertRepositoryV2.settInn(
            2018,
            4,
            13,
            ORGNR_VIRKSOMHET_1,
            NÆRINGSKODE_2SIFFER,
            NÆRINGSKODE_5SIFFER,
            3,
            1,
            3,
            16,
            100
        )
        datavarehusAggregertRepositoryV2.settInn(
            2018,
            4,
            26,
            ORGNR_VIRKSOMHET_2,
            NÆRINGSKODE_2SIFFER,
            NÆRINGSKODE_5SIFFER,
            6,
            2,
            2,
            32,
            200
        )
        datavarehusAggregertRepositoryV2.settInn(
            2019,
            4,
            13,
            ORGNR_VIRKSOMHET_2,
            NÆRINGSKODE_2SIFFER,
            NÆRINGSKODE_5SIFFER,
            10,
            2,
            4,
            20,
            100
        )
        val sykefraværsstatistikkVirksomhetMedGradering =
            repository.hentSykefraværsstatistikkVirksomhetMedGradering(ÅrstallOgKvartal(2018, 4))
        AssertionsForClassTypes.assertThat(sykefraværsstatistikkVirksomhetMedGradering.size).isEqualTo(2)
        val expected = SykefraværsstatistikkVirksomhetMedGradering(
            2018,
            4,
            ORGNR_VIRKSOMHET_1,
            NÆRINGSKODE_2SIFFER,
            NÆRINGSKODE_5SIFFER,
            Rectype.VIRKSOMHET.kode,
            1,
            BigDecimal("3.0"),
            3,
            13,
            BigDecimal("16.0"),
            BigDecimal("100.0")
        )
        val expectedLinje2 = SykefraværsstatistikkVirksomhetMedGradering(
            2018,
            4,
            ORGNR_VIRKSOMHET_2,
            NÆRINGSKODE_2SIFFER,
            NÆRINGSKODE_5SIFFER,
            Rectype.VIRKSOMHET.kode,
            2,
            BigDecimal("6.0"),
            2,
            26,
            BigDecimal("32.0"),
            BigDecimal("200.0")
        )
        AssertionsForClassTypes.assertThat(
            sykefraværsstatistikkVirksomhetMedGradering[0]
        ).isEqualTo(expected)
        AssertionsForClassTypes.assertThat(
            sykefraværsstatistikkVirksomhetMedGradering[1]
        ).isEqualTo(expectedLinje2)
    }

    @Test
    fun hentSykefraværsstatistikkLand__returnerer_en_tom_liste_dersom_ingen_data_finnes_i_DVH() {
        datavarehusLandRespository.settInn(
            2019,
            1,
            1,
            1,
            10
        )
        val sykefraværsstatistikkLand = datavarehusLandRespository.hentFor(ÅrstallOgKvartal(2018, 4))
        Assertions.assertTrue(sykefraværsstatistikkLand.isEmpty())
    }

    @Test
    fun hentVirksomhetMetadataEksportering__returnerer_virksomhetMetadataEksportering() {
        datavarehusAggregertRepositoryV2.settInn(
            ORGNR_VIRKSOMHET_1,
            Rectype.VIRKSOMHET.kode,
            Sektor.PRIVAT.sektorkode,
            NÆRINGSKODE_2SIFFER,
            "10.111",
            2020,
            3
        )
        val orgenhetList = repository.hentVirksomheter(ÅrstallOgKvartal(2020, 3))
        Assertions.assertTrue(
            orgenhetList.contains(
                Orgenhet(
                    Orgnr(ORGNR_VIRKSOMHET_1),
                    "",
                    Rectype.VIRKSOMHET.kode,
                    Sektor.PRIVAT,
                    NÆRINGSKODE_2SIFFER,
                    "10111",
                    ÅrstallOgKvartal(2020, 3)
                )
            )
        )
    }

    private fun DatavarehusAggregertRepositoryV2.settInn(
        årstall: Int,
        kvartal: Int,
        antallPersoner: Int,
        orgnrVirksomhet1: String,
        næringskode2siffer: String,
        næringskode5siffer: String,
        tapteDagsverkGradertSykemelding: Long,
        antallGradertSykemeldinger: Int,
        antallSykemeldinger: Int,
        tapteDagsverk: Long,
        muligeDagsverk: Long,
        rectype: String = Rectype.VIRKSOMHET.kode,
    ) {
        transaction {
            insert {
                it[this.årstall] = årstall
                it[this.kvartal] = kvartal
                it[this.antallPersoner] = antallPersoner
                it[this.orgnr] = orgnrVirksomhet1
                it[this.næring] = næringskode2siffer
                it[this.næringskode] = næringskode5siffer
                it[this.tapteDagsverkGradert] = tapteDagsverkGradertSykemelding.toDouble()
                it[this.antallGraderteSykemeldinger] = antallGradertSykemeldinger
                it[this.antallSykemeldinger] = antallSykemeldinger
                it[this.tapteDagsverk] = tapteDagsverk.toDouble()
                it[this.muligeDagsverk] = muligeDagsverk.toDouble()
                it[this.rectype] = rectype
            }
        }
    }

    private fun DatavarehusAggregertRepositoryV2.settInn(
        orgnr: String,
        rectype: String,
        sektor: String,
        næring: String,
        primærnæringskode: String,
        årstall: Int,
        kvartal: Int
    ) {
        transaction {
            insert {
                it[this.orgnr] = orgnr
                it[this.rectype] = rectype
                it[this.sektor] = sektor
                it[this.næring] = næring
                it[this.primærnæringskode] = primærnæringskode
                it[this.årstall] = årstall
                it[this.kvartal] = kvartal
            }
        }
    }

    private fun DatavarehusLandRespository.settInn(
        inÅrstall: Int,
        inKvartal: Int,
        inAntallPersoner: Int,
        inTaptedagsverk: Long,
        inMuligedagsverk: Long,
    ) {
        transaction {
            insert {
                it[årstall] = inÅrstall
                it[kvartal] = inKvartal
                it[antallPersoner] = inAntallPersoner
                it[tapteDagsverk] = inTaptedagsverk.toDouble()
                it[muligeDagsverk] = inMuligedagsverk.toDouble()
                it[sektor] = "1"
                it[kjønn] = "M"
                it[næring] = "41"
                it[alder] = "D"
                it[fylke] = "06"
                it[varighet] = "B"
            }
        }
    }
}
