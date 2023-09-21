package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert

import ia.felles.definisjoner.bransjer.Bransjer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.GraderingTestUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.LocalOgUnitTestOidcConfiguration
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.GraderingRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
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
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class, LocalOgUnitTestOidcConfiguration::class])
open class GraderingRepositoryJdbcTest {
    @Autowired
    private val jdbcTemplate: NamedParameterJdbcTemplate? = null
    private var graderingRepository: GraderingRepository? = null

    @BeforeEach
    fun setUp() {
        graderingRepository = GraderingRepository(jdbcTemplate)
        TestUtils.slettAllStatistikkFraDatabase(jdbcTemplate)
    }

    @AfterEach
    fun tearDown() {
        TestUtils.slettAllStatistikkFraDatabase(jdbcTemplate)
    }

    @Test
    fun hentSykefraværForEttKvartalMedGradering__skal_returnere_riktig_sykefravær() {
        GraderingTestUtils.insertDataMedGradering(
            jdbcTemplate,
            UNDERENHET_1_NÆRING_14.orgnr.verdi,
            PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
            "14100",
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            _2019_4,
            7,
            BigDecimal(10),
            BigDecimal(20),
            BigDecimal(100)
        )
        GraderingTestUtils.insertDataMedGradering(
            jdbcTemplate,
            UNDERENHET_1_NÆRING_14.orgnr.verdi,
            PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
            "14222",
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            _2019_4,
            7,
            BigDecimal(12),
            BigDecimal(20),
            BigDecimal(100)
        )
        GraderingTestUtils.insertDataMedGradering(
            jdbcTemplate,
            UNDERENHET_1_NÆRING_14.orgnr.verdi,
            PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
            "14222",
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            _2020_1,
            15,
            BigDecimal(25),
            BigDecimal(50),
            BigDecimal(300)
        )
        val resultat = graderingRepository!!.hentSykefraværMedGradering(UNDERENHET_1_NÆRING_14)
        Assertions.assertThat(resultat.size).isEqualTo(2)
        Assertions.assertThat(resultat[0])
            .isEqualTo(
                UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(2019, 4), BigDecimal(22), BigDecimal(40), 14
                )
            )
        Assertions.assertThat(resultat[1])
            .isEqualTo(
                UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(2020, 1), BigDecimal(25), BigDecimal(50), 15
                )
            )
    }

    @Test
    fun hentSykefraværForEttKvartalMedGradering__skal_returnere_riktig_underenhet_sykefravær() {
        GraderingTestUtils.insertDataMedGradering(
            jdbcTemplate,
            UNDERENHET_1_NÆRING_14.orgnr.verdi,
            PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
            UNDERENHET_1_NÆRING_14.næringskode.femsifferIdentifikator,
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            _2019_4,
            7,
            BigDecimal(10),
            BigDecimal(20),
            BigDecimal(100)
        )
        GraderingTestUtils.insertDataMedGradering(
            jdbcTemplate,
            UNDERENHET_1_NÆRING_14.orgnr.verdi,
            PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
            "14222",
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            _2020_1,
            7,
            BigDecimal(12),
            BigDecimal(20),
            BigDecimal(100)
        )
        GraderingTestUtils.insertDataMedGradering(
            jdbcTemplate,
            UNDERENHET_3_NÆRING_14.orgnr.verdi,
            PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
            "14222",
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            _2020_1,
            15,
            BigDecimal(25),
            BigDecimal(50),
            BigDecimal(300)
        )
        val resultat = graderingRepository!!.hentSykefraværMedGradering(UNDERENHET_1_NÆRING_14)
        Assertions.assertThat(resultat.size).isEqualTo(2)
        Assertions.assertThat(resultat[0])
            .isEqualTo(
                UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(2019, 4), BigDecimal(10), BigDecimal(20), 7
                )
            )
        Assertions.assertThat(resultat[1])
            .isEqualTo(
                UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(2020, 1), BigDecimal(12), BigDecimal(20), 7
                )
            )
    }

    @Test
    fun hentSykefraværForEttKvartalMedGradering__skal_returnere_riktig_sykefravær_for_næring() {
        GraderingTestUtils.insertDataMedGradering(
            jdbcTemplate,
            UNDERENHET_1_NÆRING_14.orgnr.verdi,
            PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
            UNDERENHET_1_NÆRING_14.næringskode.femsifferIdentifikator,
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            _2019_4,
            7,
            BigDecimal(10),
            BigDecimal(20),
            BigDecimal(100)
        )
        GraderingTestUtils.insertDataMedGradering(
            jdbcTemplate,
            UNDERENHET_3_NÆRING_14.orgnr.verdi,
            PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
            "14222",
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            _2020_1,
            7,
            BigDecimal(12),
            BigDecimal(20),
            BigDecimal(100)
        )
        GraderingTestUtils.insertDataMedGradering(
            jdbcTemplate,
            UNDERENHET_2_NÆRING_15.orgnr.verdi,
            PRODUKSJON_AV_LÆR_OG_LÆRVARER.tosifferIdentifikator,
            "15333",
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            _2020_1,
            15,
            BigDecimal(25),
            BigDecimal(50),
            BigDecimal(300)
        )
        val resultat = graderingRepository!!.hentSykefraværMedGradering(PRODUKSJON_AV_KLÆR)
        Assertions.assertThat(resultat.size).isEqualTo(2)
        Assertions.assertThat(resultat[0])
            .isEqualTo(
                UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(2019, 4), BigDecimal(10), BigDecimal(20), 7
                )
            )
        Assertions.assertThat(resultat[1])
            .isEqualTo(
                UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(2020, 1), BigDecimal(12), BigDecimal(20), 7
                )
            )
    }

    @Test
    fun hentSykefraværForEttKvartalMedGradering__skal_returnere_riktig_sykefravær_for_bransje() {
        val (kode) = Næringskode("86101")
        val (kode1) = Næringskode("86107")
        val (kode2) = Næringskode("86902")
        GraderingTestUtils.insertDataMedGradering(
            jdbcTemplate,
            UNDERENHET_1_NÆRING_14.orgnr.verdi,
            HELSETJENESTER.tosifferIdentifikator,
            kode,
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            _2019_4,
            7,
            BigDecimal(10),
            BigDecimal(20),
            BigDecimal(100)
        )
        GraderingTestUtils.insertDataMedGradering(
            jdbcTemplate,
            UNDERENHET_3_NÆRING_14.orgnr.verdi,
            HELSETJENESTER.tosifferIdentifikator,
            kode,
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            _2020_1,
            7,
            BigDecimal(12),
            BigDecimal(20),
            BigDecimal(100)
        )
        GraderingTestUtils.insertDataMedGradering(
            jdbcTemplate,
            UNDERENHET_2_NÆRING_15.orgnr.verdi,
            HELSETJENESTER.tosifferIdentifikator,
            kode1,
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            _2020_1,
            15,
            BigDecimal(25),
            BigDecimal(50),
            BigDecimal(300)
        )
        GraderingTestUtils.insertDataMedGradering(
            jdbcTemplate,
            UNDERENHET_2_NÆRING_15.orgnr.verdi,
            HELSETJENESTER.tosifferIdentifikator,
            kode2,
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            _2020_1,
            4,
            BigDecimal(55),
            BigDecimal(66),
            BigDecimal(3000)
        )
        val resultat = graderingRepository!!.hentSykefraværMedGradering(
            Bransje(Bransjer.SYKEHUS)
        )
        Assertions.assertThat(resultat.size).isEqualTo(2)
        Assertions.assertThat(resultat[0])
            .isEqualTo(
                UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(2019, 4), BigDecimal(10), BigDecimal(20), 7
                )
            )
        Assertions.assertThat(resultat[1])
            .isEqualTo(
                UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(2020, 1), BigDecimal(37), BigDecimal(70), 22
                )
            )
    }

    @Test
    fun hentSykefraværForEttKvartalMedGradering__henterIkkeUtGradertSykefraværForOverordnetEnhet() {
        val (kode) = Næringskode("86101")
        GraderingTestUtils.insertDataMedGradering(
            jdbcTemplate,
            OVERORDNETENHET_1_NÆRING_86.orgnr.verdi,
            HELSETJENESTER.tosifferIdentifikator,
            kode,
            DatavarehusRepository.RECTYPE_FOR_FORETAK,
            _2020_1,
            7,
            BigDecimal(10),
            BigDecimal(20),
            BigDecimal(100)
        )
        GraderingTestUtils.insertDataMedGradering(
            jdbcTemplate,
            UNDERENHET_3_NÆRING_14.orgnr.verdi,
            HELSETJENESTER.tosifferIdentifikator,
            kode,
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            _2020_1,
            7,
            BigDecimal(12),
            BigDecimal(20),
            BigDecimal(100)
        )
        val resultat = graderingRepository!!.hentSykefraværMedGradering(Bransje(Bransjer.SYKEHUS))
        Assertions.assertThat(resultat.size).isEqualTo(1)
        Assertions.assertThat(resultat[0])
            .isEqualTo(
                UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(2020, 1), BigDecimal(12), BigDecimal(20), 7
                )
            )
    }

    companion object {
        private val PRODUKSJON_AV_KLÆR = Næring("14")
        private val PRODUKSJON_AV_LÆR_OG_LÆRVARER = Næring("15")
        private val HELSETJENESTER = Næring("86")

        private val OVERORDNETENHET_1_NÆRING_86 = OverordnetEnhet(
            orgnr = Orgnr("999999777"),
            navn = "Hospital",
            næringskode = Næringskode("86101"),
            sektor = Sektor.PRIVAT,
        )

        private val UNDERENHET_1_NÆRING_14 = UnderenhetLegacy(
            Orgnr("999999999"),
            null,
            null.toString(),
            Næringskode("14120"),
            null
        )
        private val UNDERENHET_2_NÆRING_15 = UnderenhetLegacy(
            Orgnr("888888888"),
            null,
            null.toString(),
            Næringskode("15100"),
            null
        )
        private val UNDERENHET_3_NÆRING_14 = UnderenhetLegacy(
            Orgnr("777777777"),
            null,
            null.toString(),
            Næringskode("14120"),
            null
        )
        private val _2020_1 = ÅrstallOgKvartal(2020, 1)
        private val _2019_4 = ÅrstallOgKvartal(2019, 4)
    }
}
