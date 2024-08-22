package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import ia.felles.definisjoner.bransjer.Bransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.Rectype
import org.assertj.core.api.Assertions.*
import org.jetbrains.exposed.sql.deleteAll
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
        with(sykefravarStatistikkVirksomhetGraderingRepository) { transaction { deleteAll() } }
    }

    @AfterEach
    fun tearDown() {
        with(sykefravarStatistikkVirksomhetGraderingRepository) { transaction { deleteAll() } }
    }

    @Test
    fun `hentSykefraværForEttKvartalMedGradering skal returnere riktig sykefravær`() {
        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = _2019_4.årstall,
                    kvartal = _2019_4.kvartal,
                    orgnr = UNDERENHET_1_NÆRING_14.orgnr.verdi,
                    næring = "14",
                    næringkode = "14100",
                    rectype = Rectype.VIRKSOMHET.kode,
                    tapteDagsverk = 20.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal(),
                    antallPersoner = 7,
                    tapteDagsverkGradertSykemelding = 10.toBigDecimal(),
                ),
                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = _2019_4.årstall,
                    kvartal = _2019_4.kvartal,
                    orgnr = UNDERENHET_1_NÆRING_14.orgnr.verdi,
                    næring = PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
                    rectype = Rectype.VIRKSOMHET.kode,
                    næringkode = "14222",
                    antallPersoner = 7,
                    tapteDagsverkGradertSykemelding = BigDecimal(12),
                    tapteDagsverk = BigDecimal(20),
                    muligeDagsverk = BigDecimal(100),
                ),
                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = _2020_1.årstall,
                    kvartal = _2020_1.kvartal,
                    orgnr = UNDERENHET_1_NÆRING_14.orgnr.verdi,
                    næring = PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
                    næringkode = "14222",
                    rectype = Rectype.VIRKSOMHET.kode,
                    antallPersoner = 15,
                    tapteDagsverkGradertSykemelding = BigDecimal(25),
                    tapteDagsverk = BigDecimal(50),
                    muligeDagsverk = BigDecimal(300),
                )
            )
        )
        val resultat = sykefravarStatistikkVirksomhetGraderingRepository.hentForOrgnr(
            UNDERENHET_1_NÆRING_14.orgnr
        )
        assertThat(resultat.size).isEqualTo(2)
        assertThat(resultat[0])
            .isEqualTo(
                UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(2019, 4), BigDecimal(22), BigDecimal(40), 14
                )
            )
        assertThat(resultat[1])
            .isEqualTo(
                UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(2020, 1), BigDecimal(25), BigDecimal(50), 15
                )
            )
    }

    @Test
    fun `hentSykefraværForEttKvartalMedGradering skal returnere riktig underenhet sykefravær`() {
        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = _2019_4.årstall,
                    kvartal = _2019_4.kvartal,
                    orgnr = UNDERENHET_1_NÆRING_14.orgnr.verdi,
                    næring = PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
                    næringkode = UNDERENHET_1_NÆRING_14.næringskode.femsifferIdentifikator,
                    rectype = Rectype.VIRKSOMHET.kode,
                    antallPersoner = 7,
                    tapteDagsverkGradertSykemelding = BigDecimal(10),
                    tapteDagsverk = BigDecimal(20),
                    muligeDagsverk = BigDecimal(100),
                ), SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = _2020_1.årstall,
                    kvartal = _2020_1.kvartal,
                    orgnr = UNDERENHET_1_NÆRING_14.orgnr.verdi,
                    næring = PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
                    næringkode = "14222",
                    rectype = Rectype.VIRKSOMHET.kode,
                    antallPersoner = 7,
                    tapteDagsverkGradertSykemelding = BigDecimal(12),
                    tapteDagsverk = BigDecimal(20),
                    muligeDagsverk = BigDecimal(100),
                ), SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = _2020_1.årstall,
                    kvartal = _2020_1.kvartal,
                    orgnr = UNDERENHET_3_NÆRING_14.orgnr.verdi,
                    næring = PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
                    næringkode = "14222",
                    rectype = Rectype.VIRKSOMHET.kode,
                    antallPersoner = 15,
                    tapteDagsverkGradertSykemelding = BigDecimal(25),
                    tapteDagsverk = BigDecimal(50),
                    muligeDagsverk = BigDecimal(300),
                )
            )
        )

        val resultat = sykefravarStatistikkVirksomhetGraderingRepository.hentForOrgnr(
            UNDERENHET_1_NÆRING_14.orgnr
        )
        assertThat(resultat.size).isEqualTo(2)
        assertThat(resultat[0])
            .isEqualTo(
                UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(2019, 4), BigDecimal(10), BigDecimal(20), 7
                )
            )
        assertThat(resultat[1])
            .isEqualTo(
                UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(2020, 1), BigDecimal(12), BigDecimal(20), 7
                )
            )
    }

    @Test
    fun `hentSykefraværForEttKvartalMedGradering skal returnere riktig sykefravær for næring`() {
        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = _2019_4.årstall,
                    kvartal = _2019_4.kvartal,
                    orgnr = UNDERENHET_1_NÆRING_14.orgnr.verdi,
                    næring = PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
                    næringkode = UNDERENHET_1_NÆRING_14.næringskode.femsifferIdentifikator,
                    rectype = Rectype.VIRKSOMHET.kode,
                    antallPersoner = 7,
                    tapteDagsverkGradertSykemelding = BigDecimal(10),
                    tapteDagsverk = BigDecimal(20),
                    muligeDagsverk = BigDecimal(100),
                ),
                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = _2020_1.årstall,
                    kvartal = _2020_1.kvartal,
                    orgnr = UNDERENHET_3_NÆRING_14.orgnr.verdi,
                    næring = PRODUKSJON_AV_KLÆR.tosifferIdentifikator,
                    næringkode = "14222",
                    rectype = Rectype.VIRKSOMHET.kode,
                    antallPersoner = 7,
                    tapteDagsverkGradertSykemelding = BigDecimal(12),
                    tapteDagsverk = BigDecimal(20),
                    muligeDagsverk = BigDecimal(100),
                ),
                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = _2020_1.årstall,
                    kvartal = _2020_1.kvartal,
                    orgnr = UNDERENHET_2_NÆRING_15.orgnr.verdi,
                    næring = PRODUKSJON_AV_LÆR_OG_LÆRVARER.tosifferIdentifikator,
                    næringkode = "15333",
                    rectype = Rectype.VIRKSOMHET.kode,
                    antallPersoner = 15,
                    tapteDagsverkGradertSykemelding = BigDecimal(25),
                    tapteDagsverk = BigDecimal(50),
                    muligeDagsverk = BigDecimal(300),
                )

            )
        )

        val resultat =
            sykefravarStatistikkVirksomhetGraderingRepository.hentForNæring(PRODUKSJON_AV_KLÆR)
        assertThat(resultat.size).isEqualTo(2)
        assertThat(resultat[0])
            .isEqualTo(
                UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(2019, 4), BigDecimal(10), BigDecimal(20), 7
                )
            )
        assertThat(resultat[1])
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
            listOf(
                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = _2019_4.årstall,
                    kvartal = _2019_4.kvartal,
                    orgnr = UNDERENHET_1_NÆRING_14.orgnr.verdi,
                    næring = HELSETJENESTER.tosifferIdentifikator,
                    næringkode = kode,
                    rectype = Rectype.VIRKSOMHET.kode,
                    antallPersoner = 7,
                    tapteDagsverkGradertSykemelding = BigDecimal(10),
                    tapteDagsverk = BigDecimal(20),
                    muligeDagsverk = BigDecimal(100),
                ),
                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = _2020_1.årstall,
                    kvartal = _2020_1.kvartal,
                    orgnr = UNDERENHET_3_NÆRING_14.orgnr.verdi,
                    næring = HELSETJENESTER.tosifferIdentifikator,
                    næringkode = kode,
                    rectype = Rectype.VIRKSOMHET.kode,
                    antallPersoner = 7,
                    tapteDagsverkGradertSykemelding = BigDecimal(12),
                    tapteDagsverk = BigDecimal(20),
                    muligeDagsverk = BigDecimal(100),
                ),
                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = _2020_1.årstall,
                    kvartal = _2020_1.kvartal,
                    orgnr = UNDERENHET_2_NÆRING_15.orgnr.verdi,
                    næring = HELSETJENESTER.tosifferIdentifikator,
                    næringkode = kode1,
                    rectype = Rectype.VIRKSOMHET.kode,
                    antallPersoner = 15,
                    tapteDagsverkGradertSykemelding = BigDecimal(25),
                    tapteDagsverk = BigDecimal(50),
                    muligeDagsverk = BigDecimal(300),
                ),
                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = _2020_1.årstall,
                    kvartal = _2020_1.kvartal,
                    orgnr = UNDERENHET_2_NÆRING_15.orgnr.verdi,
                    næring = HELSETJENESTER.tosifferIdentifikator,
                    næringkode = kode2,
                    rectype = Rectype.VIRKSOMHET.kode,
                    antallPersoner = 4,
                    tapteDagsverkGradertSykemelding = BigDecimal(55),
                    tapteDagsverk = BigDecimal(66),
                    muligeDagsverk = BigDecimal(3000),
                )
            )
        )
        val resultat = sykefravarStatistikkVirksomhetGraderingRepository.hentForBransje(
            Bransje.SYKEHUS
        )
        assertThat(resultat.size).isEqualTo(2)
        assertThat(resultat[0])
            .isEqualTo(
                UmaskertSykefraværForEttKvartal(
                    ÅrstallOgKvartal(2019, 4), BigDecimal(10), BigDecimal(20), 7
                )
            )
        assertThat(resultat[1])
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
            listOf(
                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = _2020_1.årstall,
                    kvartal = _2020_1.kvartal,
                    orgnr = OVERORDNETENHET_1_NÆRING_86.orgnr.verdi,
                    næring = HELSETJENESTER.tosifferIdentifikator,
                    næringkode = kode,
                    rectype = Rectype.FORETAK.kode,
                    antallPersoner = 7,
                    tapteDagsverkGradertSykemelding = BigDecimal(10),
                    tapteDagsverk = BigDecimal(20),
                    muligeDagsverk = BigDecimal(100),
                ),
                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = _2020_1.årstall,
                    kvartal = _2020_1.kvartal,
                    orgnr = UNDERENHET_3_NÆRING_14.orgnr.verdi,
                    næring = HELSETJENESTER.tosifferIdentifikator,
                    næringkode = kode,
                    rectype = Rectype.VIRKSOMHET.kode,
                    antallPersoner = 7,
                    tapteDagsverkGradertSykemelding = BigDecimal(12),
                    tapteDagsverk = BigDecimal(20),
                    muligeDagsverk = BigDecimal(100),
                )
            )
        )
        val resultat = sykefravarStatistikkVirksomhetGraderingRepository.hentForBransje(Bransje.SYKEHUS)
        assertThat(resultat.size).isEqualTo(1)
        assertThat(resultat[0])
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
