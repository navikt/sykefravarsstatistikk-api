package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.Orgenhet
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
    private lateinit var datavarehusAggregertRepositoryV1: DatavarehusAggregertRepositoryV1

    private val ORGNR_VIRKSOMHET_1 = "987654321"
    private val ORGNR_VIRKSOMHET_2 = "999999999"
    private val ORGNR_VIRKSOMHET_3 = "999999777"
    private val NÆRINGSKODE_5SIFFER = "10062"
    private val NÆRINGSKODE_2SIFFER = "10"

    @BeforeEach
    fun setUp() {
        cleanUpTestDb(
            datavarehusLandRespository = datavarehusLandRespository,
            datavarehusAggregertRepositoryV2 = datavarehusAggregertRepositoryV2,
            datavarehusAggregertRepositoryV1 = datavarehusAggregertRepositoryV1,
        )
    }

    @Test
    fun hentSisteÅrstallOgKvartalFraSykefraværsstatistikk__returnerer_siste_ÅrstallOgKvartal_for_Land_og_sektor() {
        datavarehusLandRespository.settInn(
            inÅrstall = 2019,
            inKvartal = 4,
            inAntallPersoner = 4,
            inTaptedagsverk = 5,
            inMuligedagsverk = 100
        )
        datavarehusLandRespository.settInn(
            inÅrstall = 2019,
            inKvartal = 4,
            inAntallPersoner = 6,
            inTaptedagsverk = 10,
            inMuligedagsverk = 100
        )
        datavarehusLandRespository.settInn(
            inÅrstall = 2020,
            inKvartal = 1,
            inAntallPersoner = 1,
            inTaptedagsverk = 1,
            inMuligedagsverk = 10
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
    fun hentNæring5Siffer__returnerer_Næring5Siffer_for_årstall_og_kvartal() {
        insertSykefraværsstatistikkNærin5SiffergInDvhTabell(
            namedParameterJdbcTemplate, 2022, 3, 4, "01110", "K", 5, 100
        )
        insertSykefraværsstatistikkNærin5SiffergInDvhTabell(
            namedParameterJdbcTemplate, 2022, 3, 4, "80830", "M", 25, 1300
        )
        insertSykefraværsstatistikkNærin5SiffergInDvhTabell(
            namedParameterJdbcTemplate, 2020, 1, 2, "01110", "M", 12, 100
        )
        val results: List<SykefraværsstatistikkForNæringskode> =
            datavarehusNæringskodeRepository.hentFor(ÅrstallOgKvartal(2022, 3))
        AssertionsForClassTypes.assertThat(results.size).isEqualTo(2)
        AssertionsForClassTypes.assertThat(results.filter { it.næringskode == "80830" }.size).isEqualTo(1)
    }

    @Test
    fun hentSisteÅrstallOgKvartalFraSykefraværsstatistikk__returnerer_siste_ÅrstallOgKvartal_for_Virksomhet() {
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            jdbcTemplate = namedParameterJdbcTemplate,
            årstall = 2018,
            kvartal = 4,
            antallPersoner = 4,
            orgnr = ORGNR_VIRKSOMHET_1,
            næringskode5siffer = "10062", varighet = Varighetskategori._8_DAGER_TIL_16_DAGER,
            kjonn = "K",
            taptedagsverk = 5,
            muligedagsverk = 100
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            jdbcTemplate = namedParameterJdbcTemplate,
            årstall = 2019,
            kvartal = 1,
            antallPersoner = 5,
            orgnr = ORGNR_VIRKSOMHET_2,
            næringskode5siffer = "10062", varighet = Varighetskategori._8_DAGER_TIL_16_DAGER,
            kjonn = "M",
            taptedagsverk = 5,
            muligedagsverk = 101
        )
        val sisteÅrstallOgKvartal =
            datavarehusAggregertRepositoryV1.hentSisteKvartal()
        AssertionsForClassTypes.assertThat(sisteÅrstallOgKvartal).isEqualTo(ÅrstallOgKvartal(2019, 1))
    }

    @Test
    fun hentSykefraværsstatistikkSektor__lager_sum_og_returnerer_antall_tapte_og_mulige_dagsverk() {
        datavarehusLandRespository.settInn(
            inÅrstall = 2018,
            inKvartal = 4,
            inAntallPersoner = 1,
            inTaptedagsverk = 5,
            inMuligedagsverk = 100
        )
        datavarehusLandRespository.settInn(
            inÅrstall = 2018,
            inKvartal = 4,
            inAntallPersoner = 3,
            inTaptedagsverk = 10,
            inMuligedagsverk = 100
        )
        datavarehusLandRespository.settInn(
            inÅrstall = 2019,
            inKvartal = 1,
            inAntallPersoner = 1,
            inTaptedagsverk = 1,
            inMuligedagsverk = 10
        )
        val sykefraværsstatistikkSektor = datavarehusLandRespository.hentSykefraværsstatistikkSektor(
            ÅrstallOgKvartal(2018, 4),
        )
        AssertionsForClassTypes.assertThat(sykefraværsstatistikkSektor.size).isEqualTo(1)
        val sykefraværsstatistikkSektorExpected =
            SykefraværsstatistikkSektor(2018, 4, Sektor.STATLIG, 4, BigDecimal("15.0"), BigDecimal("200.0"))
        val sykefraværsstatistikkSektorActual = sykefraværsstatistikkSektor[0]
        Assertions.assertTrue(
            ReflectionEquals(sykefraværsstatistikkSektorExpected)
                .matches(sykefraværsstatistikkSektorActual)
        )
    }

    @Test
    fun hentSykefraværsstatistikkLand__lager_sum_og_returnerer_antall_tapte_og_mulige_dagsverk() {
        datavarehusLandRespository.settInn(
            inÅrstall = 2018,
            inKvartal = 4,
            inAntallPersoner = 4,
            inTaptedagsverk = 5,
            inMuligedagsverk = 100
        )
        datavarehusLandRespository.settInn(
            inÅrstall = 2018,
            inKvartal = 4,
            inAntallPersoner = 6,
            inTaptedagsverk = 10,
            inMuligedagsverk = 100
        )
        datavarehusLandRespository.settInn(
            inÅrstall = 2019,
            inKvartal = 1,
            inAntallPersoner = 1,
            inTaptedagsverk = 1,
            inMuligedagsverk = 10
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
            jdbcTemplate = namedParameterJdbcTemplate,
            årstall = 2018,
            kvartal = 4,
            antallPersoner = 4,
            orgnr = ORGNR_VIRKSOMHET_1,
            næringskode5siffer = "10062", varighet = Varighetskategori._1_DAG_TIL_7_DAGER,
            kjonn = "K",
            taptedagsverk = 5,
            muligedagsverk = 100
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            jdbcTemplate = namedParameterJdbcTemplate,
            årstall = 2018,
            kvartal = 4,
            antallPersoner = 3,
            orgnr = ORGNR_VIRKSOMHET_1,
            næringskode5siffer = "10062", varighet = Varighetskategori._1_DAG_TIL_7_DAGER,
            kjonn = "M",
            taptedagsverk = 8,
            muligedagsverk = 88
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            jdbcTemplate = namedParameterJdbcTemplate,
            årstall = 2018,
            kvartal = 4,
            antallPersoner = 6,
            orgnr = ORGNR_VIRKSOMHET_2,
            næringskode5siffer = "10062", varighet = Varighetskategori._1_DAG_TIL_7_DAGER,
            kjonn = "K",
            taptedagsverk = 3,
            muligedagsverk = 75
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            jdbcTemplate = namedParameterJdbcTemplate,
            årstall = 2019,
            kvartal = 1,
            antallPersoner = 5,
            orgnr = ORGNR_VIRKSOMHET_1,
            næringskode5siffer = "10062", varighet = Varighetskategori._1_DAG_TIL_7_DAGER,
            kjonn = "M",
            taptedagsverk = 5,
            muligedagsverk = 101
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            jdbcTemplate = namedParameterJdbcTemplate,
            årstall = 2019,
            kvartal = 2,
            antallPersoner = 9,
            orgnr = ORGNR_VIRKSOMHET_1,
            næringskode5siffer = "10062", varighet = Varighetskategori._8_DAGER_TIL_16_DAGER,
            kjonn = "M",
            taptedagsverk = 9,
            muligedagsverk = 99
        )
        val sykefraværsstatistikkVirksomhet =
            datavarehusAggregertRepositoryV1.hentSykefraværsstatistikkVirksomhet(ÅrstallOgKvartal(2018, 4))
        AssertionsForClassTypes.assertThat(sykefraværsstatistikkVirksomhet.size).isEqualTo(2)
        val expected = SykefraværsstatistikkVirksomhet(
            2018,
            4,
            ORGNR_VIRKSOMHET_1,
            Varighetskategori._1_DAG_TIL_7_DAGER.kode,
            Rectype.VIRKSOMHET.kode,
            7,
            BigDecimal("13.0"),
            BigDecimal("188.0")
        )
        AssertionsForClassTypes.assertThat(sykefraværsstatistikkVirksomhet[0]).isEqualTo(expected)
    }

    @Test
    fun hentSykefraværsstatistikkNæringMedVarighet__lager_sum_og_returnerer_antall_tapte_og_mulige_dagsverk_med_varighet() {
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            jdbcTemplate = namedParameterJdbcTemplate,
            årstall = 2018,
            kvartal = 4,
            antallPersoner = 4,
            orgnr = ORGNR_VIRKSOMHET_1,
            næringskode5siffer = "10062", varighet = Varighetskategori._1_DAG_TIL_7_DAGER,
            kjonn = "K",
            taptedagsverk = 5,
            muligedagsverk = 100
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            jdbcTemplate = namedParameterJdbcTemplate,
            årstall = 2018,
            kvartal = 4,
            antallPersoner = 4,
            orgnr = ORGNR_VIRKSOMHET_1,
            næringskode = "10062", varighet = Varighetskategori._1_DAG_TIL_7_DAGER,
            kjonn = "K",
            taptedagsverk = 1,
            muligedagsverk = 10,
            rectype = Rectype.FORETAK.kode
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            jdbcTemplate = namedParameterJdbcTemplate,
            årstall = 2018,
            kvartal = 4,
            antallPersoner = 4,
            orgnr = ORGNR_VIRKSOMHET_1,
            næringskode5siffer = "10062", varighet = Varighetskategori._8_DAGER_TIL_16_DAGER,
            kjonn = "K",
            taptedagsverk = 5,
            muligedagsverk = 100
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            jdbcTemplate = namedParameterJdbcTemplate,
            årstall = 2018,
            kvartal = 4,
            antallPersoner = 3,
            orgnr = ORGNR_VIRKSOMHET_1,
            næringskode5siffer = "10062", varighet = Varighetskategori._1_DAG_TIL_7_DAGER,
            kjonn = "M",
            taptedagsverk = 8,
            muligedagsverk = 88
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            jdbcTemplate = namedParameterJdbcTemplate,
            årstall = 2018,
            kvartal = 4,
            antallPersoner = 6,
            orgnr = ORGNR_VIRKSOMHET_2,
            næringskode5siffer = "10062", varighet = Varighetskategori._1_DAG_TIL_7_DAGER,
            kjonn = "K",
            taptedagsverk = 3,
            muligedagsverk = 75
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            jdbcTemplate = namedParameterJdbcTemplate,
            årstall = 2018,
            kvartal = 4,
            antallPersoner = 6,
            orgnr = ORGNR_VIRKSOMHET_3,
            næringskode5siffer = "85000", varighet = Varighetskategori._1_DAG_TIL_7_DAGER,
            kjonn = "K",
            taptedagsverk = 10,
            muligedagsverk = 80
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            jdbcTemplate = namedParameterJdbcTemplate,
            årstall = 2019,
            kvartal = 1,
            antallPersoner = 5,
            orgnr = ORGNR_VIRKSOMHET_1,
            næringskode5siffer = "10062", varighet = Varighetskategori._1_DAG_TIL_7_DAGER,
            kjonn = "M",
            taptedagsverk = 5,
            muligedagsverk = 101
        )
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
            jdbcTemplate = namedParameterJdbcTemplate,
            årstall = 2019,
            kvartal = 2,
            antallPersoner = 9,
            orgnr = ORGNR_VIRKSOMHET_3,
            næringskode5siffer = "85000", varighet = Varighetskategori._8_DAGER_TIL_16_DAGER,
            kjonn = "M",
            taptedagsverk = 9,
            muligedagsverk = 99
        )
        val sykefraværsstatistikkNæringMedVarighet =
            datavarehusAggregertRepositoryV1.hentSykefraværsstatistikkNæringMedVarighet(
                ÅrstallOgKvartal(
                    2018,
                    4
                )
            )
        AssertionsForClassTypes.assertThat(sykefraværsstatistikkNæringMedVarighet.size).isEqualTo(3)
        val expected = SykefraværsstatistikkNæringMedVarighet(
            2018,
            4,
            NÆRINGSKODE_5SIFFER,
            Varighetskategori._1_DAG_TIL_7_DAGER.kode,
            13,
            BigDecimal("16.0"),
            BigDecimal("263.0")
        )
        AssertionsForClassTypes.assertThat(
            sykefraværsstatistikkNæringMedVarighet[0]
        ).isEqualTo(expected)
    }

    @Test
    fun hentSykefraværsstatistikkVirksomhetMedGradering__lager_sum_og_returnerer_antall_tapte_dagsverk_i_gradert_sykemelding_og_mulige_dagsverk() {
        datavarehusAggregertRepositoryV2.settInn(
            årstall = 2018,
            kvartal = 4,
            antallPersoner = 13,
            orgnrVirksomhet1 = ORGNR_VIRKSOMHET_1,
            næringskode2siffer = NÆRINGSKODE_2SIFFER,
            næringskode5siffer = NÆRINGSKODE_5SIFFER,
            tapteDagsverkGradertSykemelding = 3,
            tapteDagsverk = 16,
            muligeDagsverk = 100
        )
        datavarehusAggregertRepositoryV2.settInn(
            årstall = 2018,
            kvartal = 4,
            antallPersoner = 26,
            orgnrVirksomhet1 = ORGNR_VIRKSOMHET_2,
            næringskode2siffer = NÆRINGSKODE_2SIFFER,
            næringskode5siffer = NÆRINGSKODE_5SIFFER,
            tapteDagsverkGradertSykemelding = 6,
            tapteDagsverk = 32,
            muligeDagsverk = 200
        )
        datavarehusAggregertRepositoryV2.settInn(
            årstall = 2019,
            kvartal = 4,
            antallPersoner = 13,
            orgnrVirksomhet1 = ORGNR_VIRKSOMHET_2,
            næringskode2siffer = NÆRINGSKODE_2SIFFER,
            næringskode5siffer = NÆRINGSKODE_5SIFFER,
            tapteDagsverkGradertSykemelding = 10,
            tapteDagsverk = 20,
            muligeDagsverk = 100
        )

        val sykefraværsstatistikkVirksomhetMedGradering =
            datavarehusAggregertRepositoryV2.hentSykefraværsstatistikkVirksomhetMedGradering(
                ÅrstallOgKvartal(
                    2018,
                    4
                )
            )

        AssertionsForClassTypes.assertThat(sykefraværsstatistikkVirksomhetMedGradering.size).isEqualTo(2)
        val expected = SykefraværsstatistikkVirksomhetMedGradering(
            årstall = 2018,
            kvartal = 4,
            orgnr = ORGNR_VIRKSOMHET_1,
            næring = NÆRINGSKODE_2SIFFER,
            næringkode = NÆRINGSKODE_5SIFFER,
            rectype = Rectype.VIRKSOMHET.kode,
            tapteDagsverkGradertSykemelding = BigDecimal("3.0"),
            antallPersoner = 13,
            tapteDagsverk = BigDecimal("16.0"),
            muligeDagsverk = BigDecimal("100.0")
        )
        val expectedLinje2 = SykefraværsstatistikkVirksomhetMedGradering(
            årstall = 2018,
            kvartal = 4,
            orgnr = ORGNR_VIRKSOMHET_2,
            næring = NÆRINGSKODE_2SIFFER,
            næringkode = NÆRINGSKODE_5SIFFER,
            rectype = Rectype.VIRKSOMHET.kode,
            tapteDagsverkGradertSykemelding = BigDecimal("6.0"),
            antallPersoner = 26,
            tapteDagsverk = BigDecimal("32.0"),
            muligeDagsverk = BigDecimal("200.0")
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
            inÅrstall = 2019,
            inKvartal = 1,
            inAntallPersoner = 1,
            inTaptedagsverk = 1,
            inMuligedagsverk = 10
        )
        val sykefraværsstatistikkLand = datavarehusLandRespository.hentFor(ÅrstallOgKvartal(2018, 4))
        Assertions.assertTrue(sykefraværsstatistikkLand.isEmpty())
    }

    @Test
    fun hentVirksomhetMetadataEksportering__returnerer_virksomhetMetadataEksportering() {
        datavarehusAggregertRepositoryV2.settInn(
            orgnr = ORGNR_VIRKSOMHET_1,
            rectype = Rectype.VIRKSOMHET.kode,
            sektor = Sektor.PRIVAT.sektorkode,
            næring = NÆRINGSKODE_2SIFFER,
            primærnæringskode = "10.111",
            årstall = 2020,
            kvartal = 3
        )
        val orgenhetList = datavarehusAggregertRepositoryV2.hentVirksomheter(ÅrstallOgKvartal(2020, 3))
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
