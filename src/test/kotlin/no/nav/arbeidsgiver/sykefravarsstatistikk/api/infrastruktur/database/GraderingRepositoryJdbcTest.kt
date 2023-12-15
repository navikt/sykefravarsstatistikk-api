package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import ia.felles.definisjoner.bransjer.Bransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.Rectype
import org.assertj.core.api.Assertions
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
import testUtils.TestUtils
import testUtils.settInn
import java.math.BigDecimal

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class GraderingRepositoryJdbcTest {

    @Autowired
    private lateinit var sykefravarStatistikkVirksomhetGraderingRepository: SykefravarStatistikkVirksomhetGraderingRepository

    @BeforeEach
    fun setUp() {
        TestUtils.slettAllStatistikkFraDatabase(
            sykefravarStatistikkVirksomhetGraderingRepository = sykefravarStatistikkVirksomhetGraderingRepository
        )
    }

    @AfterEach
    fun tearDown() {
        TestUtils.slettAllStatistikkFraDatabase(
            sykefravarStatistikkVirksomhetGraderingRepository = sykefravarStatistikkVirksomhetGraderingRepository
        )
    }

    @Test
    fun `hentSykefraværForEttKvartalMedGradering skal returnere riktig sykefravær`() {

        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            UNDERENHET_1_NÆRING_14.orgnr.verdi,
            PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
            "14100",
            Rectype.VIRKSOMHET.kode,
            _2019_4,
            7,
            BigDecimal(10),
            BigDecimal(20),
            BigDecimal(100)
        )
        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            UNDERENHET_1_NÆRING_14.orgnr.verdi,
            PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
            "14222",
            Rectype.VIRKSOMHET.kode,
            _2019_4,
            7,
            BigDecimal(12),
            BigDecimal(20),
            BigDecimal(100)
        )
        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            UNDERENHET_1_NÆRING_14.orgnr.verdi,
            PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
            "14222",
            Rectype.VIRKSOMHET.kode,
            _2020_1,
            15,
            BigDecimal(25),
            BigDecimal(50),
            BigDecimal(300)
        )
        val resultat = sykefravarStatistikkVirksomhetGraderingRepository.hentForOrgnr(
            UNDERENHET_1_NÆRING_14.orgnr
        )
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
        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            UNDERENHET_1_NÆRING_14.orgnr.verdi,
            PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
            UNDERENHET_1_NÆRING_14.næringskode.femsifferIdentifikator,
            Rectype.VIRKSOMHET.kode,
            _2019_4,
            7,
            BigDecimal(10),
            BigDecimal(20),
            BigDecimal(100)
        )
        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            UNDERENHET_1_NÆRING_14.orgnr.verdi,
            PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
            "14222",
            Rectype.VIRKSOMHET.kode,
            _2020_1,
            7,
            BigDecimal(12),
            BigDecimal(20),
            BigDecimal(100)
        )
        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            UNDERENHET_3_NÆRING_14.orgnr.verdi,
            PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
            "14222",
            Rectype.VIRKSOMHET.kode,
            _2020_1,
            15,
            BigDecimal(25),
            BigDecimal(50),
            BigDecimal(300)
        )
        val resultat = sykefravarStatistikkVirksomhetGraderingRepository.hentForOrgnr(
            UNDERENHET_1_NÆRING_14.orgnr
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
                    ÅrstallOgKvartal(2020, 1), BigDecimal(12), BigDecimal(20), 7
                )
            )
    }

    @Test
    fun hentSykefraværForEttKvartalMedGradering__skal_returnere_riktig_sykefravær_for_næring() {
        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            UNDERENHET_1_NÆRING_14.orgnr.verdi,
            PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
            UNDERENHET_1_NÆRING_14.næringskode.femsifferIdentifikator,
            Rectype.VIRKSOMHET.kode,
            _2019_4,
            7,
            BigDecimal(10),
            BigDecimal(20),
            BigDecimal(100)
        )
        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            UNDERENHET_3_NÆRING_14.orgnr.verdi,
            PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
            "14222",
            Rectype.VIRKSOMHET.kode,
            _2020_1,
            7,
            BigDecimal(12),
            BigDecimal(20),
            BigDecimal(100)
        )
        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            UNDERENHET_2_NÆRING_15.orgnr.verdi,
            PRODUKSJON_AV_LÆR_OG_LÆRVARER.tosifferIdentifikator,
            "15333",
            Rectype.VIRKSOMHET.kode,
            _2020_1,
            15,
            BigDecimal(25),
            BigDecimal(50),
            BigDecimal(300)
        )
        val resultat =
            sykefravarStatistikkVirksomhetGraderingRepository.hentForNæring(PRODUKSJON_AV_KLÆR)
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
        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            UNDERENHET_1_NÆRING_14.orgnr.verdi,
            HELSETJENESTER.tosifferIdentifikator,
            kode,
            Rectype.VIRKSOMHET.kode,
            _2019_4,
            7,
            BigDecimal(10),
            BigDecimal(20),
            BigDecimal(100)
        )
        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            UNDERENHET_3_NÆRING_14.orgnr.verdi,
            HELSETJENESTER.tosifferIdentifikator,
            kode,
            Rectype.VIRKSOMHET.kode,
            _2020_1,
            7,
            BigDecimal(12),
            BigDecimal(20),
            BigDecimal(100)
        )
        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            UNDERENHET_2_NÆRING_15.orgnr.verdi,
            HELSETJENESTER.tosifferIdentifikator,
            kode1,
            Rectype.VIRKSOMHET.kode,
            _2020_1,
            15,
            BigDecimal(25),
            BigDecimal(50),
            BigDecimal(300)
        )
        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            UNDERENHET_2_NÆRING_15.orgnr.verdi,
            HELSETJENESTER.tosifferIdentifikator,
            kode2,
            Rectype.VIRKSOMHET.kode,
            _2020_1,
            4,
            BigDecimal(55),
            BigDecimal(66),
            BigDecimal(3000)
        )
        val resultat = sykefravarStatistikkVirksomhetGraderingRepository.hentForBransje(
            Bransje.SYKEHUS
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
        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            OVERORDNETENHET_1_NÆRING_86.orgnr.verdi,
            HELSETJENESTER.tosifferIdentifikator,
            kode,
            Rectype.FORETAK.kode,
            _2020_1,
            7,
            BigDecimal(10),
            BigDecimal(20),
            BigDecimal(100)
        )
        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            UNDERENHET_3_NÆRING_14.orgnr.verdi,
            HELSETJENESTER.tosifferIdentifikator,
            kode,
            Rectype.VIRKSOMHET.kode,
            _2020_1,
            7,
            BigDecimal(12),
            BigDecimal(20),
            BigDecimal(100)
        )
        val resultat = sykefravarStatistikkVirksomhetGraderingRepository.hentForBransje(Bransje.SYKEHUS)
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

        private val UNDERENHET_1_NÆRING_14 = Underenhet.Næringsdrivende(
            orgnr = Orgnr("999999999"),
            overordnetEnhetOrgnr = Orgnr("999999777"),
            navn = "Klesprodusent",
            næringskode = Næringskode("14120"),
            antallAnsatte = 10
        )
        private val UNDERENHET_2_NÆRING_15 = Underenhet.Næringsdrivende(
            orgnr = Orgnr("888888888"),
            overordnetEnhetOrgnr = Orgnr("999999777"),
            navn = "Lakk og lær AS",
            næringskode = Næringskode("15100"),
            antallAnsatte = 2
        )
        private val UNDERENHET_3_NÆRING_14 = Underenhet.Næringsdrivende(
            orgnr = Orgnr("777777777"),
            overordnetEnhetOrgnr = Orgnr("999999777"),
            navn = "Svære Klær AS",
            næringskode = Næringskode("14120"),
            antallAnsatte = 1
        )
        private val _2020_1 = ÅrstallOgKvartal(2020, 1)
        private val _2019_4 = ÅrstallOgKvartal(2019, 4)
    }
}
