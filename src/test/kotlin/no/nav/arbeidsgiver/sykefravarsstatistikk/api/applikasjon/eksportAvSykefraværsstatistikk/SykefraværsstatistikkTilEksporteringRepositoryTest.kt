package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk

import config.AppConfigForJdbcTesterConfig
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
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
import testUtils.TestUtils.SISTE_PUBLISERTE_KVARTAL
import testUtils.TestUtils.opprettStatistikkForNæringer
import testUtils.TestUtils.slettAllStatistikkFraDatabase
import java.math.BigDecimal
import java.util.*
import java.util.stream.Collectors

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class SykefraværsstatistikkTilEksporteringRepositoryTest {
    @Autowired
    lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var sykefraværsstatistikkTilEksporteringRepository: SykefraværsstatistikkTilEksporteringRepository

    @Autowired
    private lateinit var sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository

    @Autowired
    lateinit var sykefraværsstatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository

    @Autowired
    lateinit var sykefraværStatistikkSektorRepository: SykefraværStatistikkSektorRepository

    @Autowired
    lateinit var sykefraværStatistikkNæringRepository: SykefraværStatistikkNæringRepository

    private val produksjonAvKlær = Næringskode("14190")
    private val undervisning = Næringskode("86907")
    private val utdanning = Næring("86")
    private val produksjon = Næring("14")
    private val VIRKSOMHET_1 = "999999999"
    private val VIRKSOMHET_2 = "999999998"

    @BeforeEach
    fun setUp() {
        slettAllStatistikkFraDatabase(
            jdbcTemplate,
            sykefraværStatistikkSektorRepository = sykefraværStatistikkSektorRepository,
            sykefraværStatistikkNæringRepository = sykefraværStatistikkNæringRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        slettAllStatistikkFraDatabase(
            jdbcTemplate,
            sykefraværStatistikkSektorRepository = sykefraværStatistikkSektorRepository,
            sykefraværStatistikkNæringRepository = sykefraværStatistikkNæringRepository,
        )
    }

    @Test
    fun hentSykefraværprosentLand__returnerer_NULL_dersom_ingen_statistikk_er_funnet_for_kvartal() {
        opprettStatistikkLandTestData()

        sykefraværStatistikkLandRepository.hentSykefraværstatistikkLand(
            listOf(
                ÅrstallOgKvartal(
                    2019,
                    4
                )
            )
        ) shouldBe emptyList()

    }

    @Test
    fun hentSykefraværprosentLand__skal_hente_sykefravær_land_for_ett_kvartal() {
        opprettStatistikkLandTestData()
        sykefraværStatistikkLandRepository.hentSykefraværstatistikkLand(
            listOf(
                ÅrstallOgKvartal(
                    2019,
                    4
                )
            )
        ) shouldBe emptyList()

        val resultat =
            sykefraværStatistikkLandRepository.hentSykefraværstatistikkLand(listOf(ÅrstallOgKvartal(2019, 2))).first()
        assertSykefraværsstatistikkIsEqual(resultat, 2019, 2, 2500000, 256800, 60000000)
        val resultat_2019_1 =
            sykefraværStatistikkLandRepository.hentSykefraværstatistikkLand(listOf(ÅrstallOgKvartal(2019, 1))).first()
        assertSykefraværsstatistikkIsEqual(resultat_2019_1, 2019, 1, 2750000, 350000, 71000000)
    }

    @Test
    fun hentSykefraværprosentAlleSektorer__skal_hente_alle_sektorer_for_ett_kvartal() {
        opprettStatistikkSektorTestData()
        val resultat =
            sykefraværStatistikkSektorRepository.hentForKvartaler(listOf(ÅrstallOgKvartal(2019, 2)))
        org.assertj.core.api.Assertions.assertThat(resultat.size).isEqualTo(2)
        assertSykefraværsstatistikkForSektorIsEqual(resultat, 2019, 2, 3, Sektor.KOMMUNAL, 1, 60)
        assertSykefraværsstatistikkForSektorIsEqual(resultat, 2019, 2, 4, Sektor.PRIVAT, 9, 100)
        val resultat_2019_1 =
            sykefraværStatistikkSektorRepository.hentForKvartaler(listOf(ÅrstallOgKvartal(2019, 1)))
        org.assertj.core.api.Assertions.assertThat(resultat_2019_1.size).isEqualTo(2)
        assertSykefraværsstatistikkForSektorIsEqual(
            resultat_2019_1, 2019, 1, 40, Sektor.KOMMUNAL, 20, 115
        )
        assertSykefraværsstatistikkForSektorIsEqual(
            resultat_2019_1, 2019, 1, 7, Sektor.PRIVAT, 12, 100
        )
    }

    @Test
    fun hentSykefraværprosentAlleNæringer__skal_hente_alle_næringer_for_ett_kvartal() {
        opprettStatistikkNæringTestData(ÅrstallOgKvartal(2019, 1), ÅrstallOgKvartal(2019, 2))
        val årstallOgKvartal = ÅrstallOgKvartal(2019, 2)
        val resultat =
            sykefraværStatistikkNæringRepository.hentForAlleNæringer(listOf(årstallOgKvartal))
        org.assertj.core.api.Assertions.assertThat(resultat.size).isEqualTo(2)
        assertSykefraværsstatistikkForNæringIsEqual(resultat, 2019, 2, 10, produksjon, 2, 100)
        assertSykefraværsstatistikkForNæringIsEqual(resultat, 2019, 2, 8, utdanning, 5, 100)
        val årstallOgKvartal1 = ÅrstallOgKvartal(2019, 1)
        val resultat_2019_1 =
            sykefraværStatistikkNæringRepository.hentForAlleNæringer(listOf(årstallOgKvartal1))
        org.assertj.core.api.Assertions.assertThat(resultat_2019_1.size).isEqualTo(2)
        assertSykefraværsstatistikkForNæringIsEqual(resultat_2019_1, 2019, 1, 10, produksjon, 2, 100)
        assertSykefraværsstatistikkForNæringIsEqual(resultat_2019_1, 2019, 1, 8, utdanning, 5, 100)
    }

    @Test
    fun hentSykefraværprosentAlleNæringer_siste4Kvartaler_skal_hente_riktig_data() {
        opprettStatistikkForNæringer(sykefraværStatistikkNæringRepository)
        val forventet = listOf(
            SykefraværsstatistikkForNæring(
                SISTE_PUBLISERTE_KVARTAL.årstall,
                SISTE_PUBLISERTE_KVARTAL.kvartal,
                "10",
                50,
                BigDecimal("20000.0"),
                BigDecimal("1000000.0")
            ),
            SykefraværsstatistikkForNæring(
                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).årstall,
                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).kvartal,
                "10",
                50,
                BigDecimal("30000.0"),
                BigDecimal("1000000.0")
            ),
            SykefraværsstatistikkForNæring(
                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).årstall,
                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).kvartal,
                "10",
                50,
                BigDecimal("40000.0"),
                BigDecimal("1000000.0")
            ),
            SykefraværsstatistikkForNæring(
                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3).årstall,
                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3).kvartal,
                "10",
                50,
                BigDecimal("50000.0"),
                BigDecimal("1000000.0")
            ),
            SykefraværsstatistikkForNæring(
                SISTE_PUBLISERTE_KVARTAL.årstall,
                SISTE_PUBLISERTE_KVARTAL.kvartal,
                "88",
                50,
                BigDecimal("25000.0"),
                BigDecimal("1000000.0")
            )
        )
        val resultat: List<SykefraværsstatistikkForNæring> =
            sykefraværStatistikkNæringRepository.hentForAlleNæringer(SISTE_PUBLISERTE_KVARTAL inkludertTidligere 3)
        org.assertj.core.api.Assertions.assertThat(resultat.size).isEqualTo(5)
        org.assertj.core.api.Assertions.assertThat(resultat).containsExactlyInAnyOrderElementsOf(forventet)
    }

    @Test
    fun hentSykefraværprosentAlleNæringer_siste4Kvartaler_kan_likevel_hente_bare_siste_publiserte_kvartal() {
        opprettStatistikkForNæringer(sykefraværStatistikkNæringRepository)
        val forventet = listOf(
            SykefraværsstatistikkForNæring(
                SISTE_PUBLISERTE_KVARTAL.årstall,
                SISTE_PUBLISERTE_KVARTAL.kvartal,
                "10",
                50,
                BigDecimal("20000.0"),
                BigDecimal("1000000.0")
            ),
            SykefraværsstatistikkForNæring(
                SISTE_PUBLISERTE_KVARTAL.årstall,
                SISTE_PUBLISERTE_KVARTAL.kvartal,
                "88",
                50,
                BigDecimal("25000.0"),
                BigDecimal("1000000.0")
            )
        )
        val resultat = sykefraværStatistikkNæringRepository.hentForAlleNæringer(listOf(SISTE_PUBLISERTE_KVARTAL))
        org.assertj.core.api.Assertions.assertThat(resultat.size).isEqualTo(2)
        org.assertj.core.api.Assertions.assertThat(resultat).containsExactlyInAnyOrderElementsOf(forventet)
    }

    @Test
    fun hentSykefraværprosentAlleNæringer_siste4Kvartaler_skalIkkeKrasjeVedManglendeData() {
        val årstallOgKvartal = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3)
        val resultat = sykefraværStatistikkNæringRepository.hentForAlleNæringer(
            listOf(
                årstallOgKvartal
            )
        )
        org.assertj.core.api.Assertions.assertThat(resultat.size).isEqualTo(0)
        org.assertj.core.api.Assertions.assertThat(resultat).containsExactlyInAnyOrderElementsOf(listOf())
    }

    @Test
    fun hentSykefraværAlleNæringer_siste4Kvartaler_skal_hente_riktig_data() {
        opprettStatistikkForNæringer(sykefraværStatistikkNæringRepository)
        val forventet = java.util.List.of<SykefraværsstatistikkForNæring>(
            SykefraværsstatistikkForNæring(
                SISTE_PUBLISERTE_KVARTAL.årstall,
                SISTE_PUBLISERTE_KVARTAL.kvartal,
                "10",
                50,
                BigDecimal("20000.0"),
                BigDecimal("1000000.0")
            ),
            SykefraværsstatistikkForNæring(
                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).årstall,
                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).kvartal,
                "10",
                50,
                BigDecimal("30000.0"),
                BigDecimal("1000000.0")
            ),
            SykefraværsstatistikkForNæring(
                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).årstall,
                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).kvartal,
                "10",
                50,
                BigDecimal("40000.0"),
                BigDecimal("1000000.0")
            ),
            SykefraværsstatistikkForNæring(
                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3).årstall,
                SISTE_PUBLISERTE_KVARTAL.minusKvartaler(3).kvartal,
                "10",
                50,
                BigDecimal("50000.0"),
                BigDecimal("1000000.0")
            ),
            SykefraværsstatistikkForNæring(
                SISTE_PUBLISERTE_KVARTAL.årstall,
                SISTE_PUBLISERTE_KVARTAL.kvartal,
                "88",
                50,
                BigDecimal("25000.0"),
                BigDecimal("1000000.0")
            )
        )
        val resultat = sykefraværStatistikkNæringRepository.hentForAlleNæringer(
            SISTE_PUBLISERTE_KVARTAL inkludertTidligere 3
        )
        org.assertj.core.api.Assertions.assertThat(resultat.size).isEqualTo(5)
        org.assertj.core.api.Assertions.assertThat(resultat).containsExactlyInAnyOrderElementsOf(forventet)
    }

    @Test
    fun hentSykefraværAlleNæringer_siste4Kvartaler_kan_likevel_hente_bare_siste_publiserte_kvartal() {
        opprettStatistikkForNæringer(sykefraværStatistikkNæringRepository)
        val forventet = java.util.List.of<SykefraværsstatistikkForNæring>(
            SykefraværsstatistikkForNæring(
                SISTE_PUBLISERTE_KVARTAL.årstall,
                SISTE_PUBLISERTE_KVARTAL.kvartal,
                "10",
                50,
                BigDecimal("20000.0"),
                BigDecimal("1000000.0")
            ),
            SykefraværsstatistikkForNæring(
                SISTE_PUBLISERTE_KVARTAL.årstall,
                SISTE_PUBLISERTE_KVARTAL.kvartal,
                "88",
                50,
                BigDecimal("25000.0"),
                BigDecimal("1000000.0")
            )
        )
        val resultat = sykefraværStatistikkNæringRepository.hentForAlleNæringer(
            listOf(
                SISTE_PUBLISERTE_KVARTAL
            )
        )
        org.assertj.core.api.Assertions.assertThat(resultat.size).isEqualTo(2)
        org.assertj.core.api.Assertions.assertThat(resultat).containsExactlyInAnyOrderElementsOf(forventet)
    }

    @Test
    fun hentSykefraværAlleNæringer_siste4Kvartaler_skalIkkeKrasjeVedManglendeData() {
        val resultat = sykefraværStatistikkNæringRepository.hentForAlleNæringer(
            SISTE_PUBLISERTE_KVARTAL inkludertTidligere 3
        )
        org.assertj.core.api.Assertions.assertThat(resultat.size).isEqualTo(0)
        org.assertj.core.api.Assertions.assertThat(resultat).containsExactlyInAnyOrderElementsOf(listOf())
    }

    @Test
    fun hentSykefraværprosentAlleNæringer5SifferForEttKvartal__skal_returnere_riktig_data_til_alle_næringer() {
        opprettStatistikkNæring5SifferTestData(
            ÅrstallOgKvartal(2019, 2), ÅrstallOgKvartal(2019, 1)
        )
        val resultat = sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentForAlleNæringskoder(
            ÅrstallOgKvartal(
                2019,
                2
            )
        )
        assertSykefraværsstatistikkForBedreNæringskodeContains(
            resultat, 2019, 2, 10, produksjonAvKlær, 3, 100
        )
        assertSykefraværsstatistikkForBedreNæringskodeContains(
            resultat, 2019, 2, 10, undervisning, 5, 100
        )
        val resultat_2019_1 = sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentForAlleNæringskoder(
            ÅrstallOgKvartal(
                2019,
                1
            )
        )
        org.assertj.core.api.Assertions.assertThat(resultat_2019_1.size).isEqualTo(2)
        assertSykefraværsstatistikkForBedreNæringskodeContains(
            resultat_2019_1, 2019, 1, 10, produksjonAvKlær, 3, 100
        )
        assertSykefraværsstatistikkForBedreNæringskodeContains(
            resultat_2019_1, 2019, 1, 10, undervisning, 5, 100
        )
    }

    @Test
    fun hentSykefraværprosentAlleNæringer5SifferForSiste4Kvartaler__skal_returnere_riktig_data_til_alle_næringer() {
        opprettStatistikkNæring5SifferTestData(
            ÅrstallOgKvartal(2019, 2), ÅrstallOgKvartal(2019, 1)
        )
        val resultat = sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentForAlleNæringskoder(
            ÅrstallOgKvartal(
                2019,
                2
            )
        )
        assertSykefraværsstatistikkForBedreNæringskodeContains(
            resultat, 2019, 2, 10, produksjonAvKlær, 3, 100
        )
        assertSykefraværsstatistikkForBedreNæringskodeContains(
            resultat, 2019, 2, 10, undervisning, 5, 100
        )
        val resultat_2019_1_til_2019_2 =
            sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentForAlleNæringskoder(
                ÅrstallOgKvartal(2019, 1), ÅrstallOgKvartal(2019, 2)
            )
        org.assertj.core.api.Assertions.assertThat(resultat.size).isEqualTo(2)
        org.assertj.core.api.Assertions.assertThat(resultat_2019_1_til_2019_2.size).isEqualTo(4)
        assertSykefraværsstatistikkForBedreNæringskodeContains(
            resultat_2019_1_til_2019_2, 2019, 1, 10, produksjonAvKlær, 3, 100
        )
        assertSykefraværsstatistikkForBedreNæringskodeContains(
            resultat_2019_1_til_2019_2, 2019, 1, 10, undervisning, 5, 100
        )
        assertSykefraværsstatistikkForBedreNæringskodeContains(
            resultat_2019_1_til_2019_2, 2019, 2, 10, produksjonAvKlær, 3, 100
        )
        assertSykefraværsstatistikkForBedreNæringskodeContains(
            resultat_2019_1_til_2019_2, 2019, 2, 10, undervisning, 5, 100
        )
    }

    @Test
    fun hentSykefraværprosentAlleVirksomheter__skal_hente_alle_virksomheter_for_ett_eller_flere_kvartaler() {
        opprettStatistikkVirksomhetTestData()
        val resultat_2019_2 = sykefraværsstatistikkVirksomhetRepository.hentSykefraværAlleVirksomheter(
            listOf(
                ÅrstallOgKvartal(2019, 2)
            )
        )
        org.assertj.core.api.Assertions.assertThat(resultat_2019_2.size).isEqualTo(2)
        assertSykefraværsstatistikkForVirksomhetIsEqual(
            resultat_2019_2, 2019, 2, 3, VIRKSOMHET_1, 1, 60
        )
        assertSykefraværsstatistikkForVirksomhetIsEqual(
            resultat_2019_2, 2019, 2, 4, VIRKSOMHET_2, 9, 100
        )
        val resultat_2019_1_TIL_2019_2 =
            sykefraværsstatistikkVirksomhetRepository.hentSykefraværAlleVirksomheter(
                ÅrstallOgKvartal(
                    2019,
                    2
                ) inkludertTidligere 1
            )
        org.assertj.core.api.Assertions.assertThat(resultat_2019_1_TIL_2019_2.size).isEqualTo(4)
        assertSykefraværsstatistikkForVirksomhetIsEqual(
            resultat_2019_1_TIL_2019_2, 2019, 1, 40, VIRKSOMHET_1, 20, 115
        )
        assertSykefraværsstatistikkForVirksomhetIsEqual(
            resultat_2019_1_TIL_2019_2, 2019, 1, 7, VIRKSOMHET_2, 12, 100
        )
        assertSykefraværsstatistikkForVirksomhetIsEqual(
            resultat_2019_1_TIL_2019_2, 2019, 2, 3, VIRKSOMHET_1, 1, 60
        )
        assertSykefraværsstatistikkForVirksomhetIsEqual(
            resultat_2019_1_TIL_2019_2, 2019, 2, 4, VIRKSOMHET_2, 9, 100
        )
        val resultat_2018_3_TIL_2019_2 =
            sykefraværsstatistikkVirksomhetRepository.hentSykefraværAlleVirksomheter(
                ÅrstallOgKvartal(
                    2019,
                    2
                ) inkludertTidligere 3
            )
        org.assertj.core.api.Assertions.assertThat(resultat_2018_3_TIL_2019_2.size).isEqualTo(8)
        assertSykefraværsstatistikkForVirksomhetIsEqual(
            resultat_2018_3_TIL_2019_2, 2018, 4, 40, VIRKSOMHET_1, 20, 115
        )
        assertSykefraværsstatistikkForVirksomhetIsEqual(
            resultat_2018_3_TIL_2019_2, 2018, 4, 7, VIRKSOMHET_2, 12, 100
        )
        assertSykefraværsstatistikkForVirksomhetIsEqual(
            resultat_2018_3_TIL_2019_2, 2018, 3, 3, VIRKSOMHET_1, 1, 60
        )
        assertSykefraværsstatistikkForVirksomhetIsEqual(
            resultat_2018_3_TIL_2019_2, 2018, 3, 4, VIRKSOMHET_2, 9, 100
        )
        assertSykefraværsstatistikkForVirksomhetIsEqual(
            resultat_2018_3_TIL_2019_2, 2019, 1, 40, VIRKSOMHET_1, 20, 115
        )
        assertSykefraværsstatistikkForVirksomhetIsEqual(
            resultat_2018_3_TIL_2019_2, 2019, 1, 7, VIRKSOMHET_2, 12, 100
        )
        assertSykefraværsstatistikkForVirksomhetIsEqual(
            resultat_2018_3_TIL_2019_2, 2019, 2, 3, VIRKSOMHET_1, 1, 60
        )
        assertSykefraværsstatistikkForVirksomhetIsEqual(
            resultat_2018_3_TIL_2019_2, 2019, 2, 4, VIRKSOMHET_2, 9, 100
        )
    }

    private fun assertSykefraværsstatistikkForSektorIsEqual(
        actual: List<SykefraværsstatistikkSektor>,
        årstall: Int,
        kvartal: Int,
        antallPersoner: Int,
        sektor: Sektor,
        tapteDagsverk: Int,
        muligeDagsverk: Int
    ) {
        val statistikkForSektor = actual.stream()
            .filter { (_, _, sektorkode): SykefraværsstatistikkSektor -> sektorkode == sektor.sektorkode }
            .toList()
        org.assertj.core.api.Assertions.assertThat(statistikkForSektor.size).isEqualTo(1)
        assertSykefraværsstatistikkIsEqual(
            statistikkForSektor[0],
            årstall,
            kvartal,
            antallPersoner,
            tapteDagsverk,
            muligeDagsverk
        )
    }

    private fun assertSykefraværsstatistikkForVirksomhetIsEqual(
        actual: List<SykefraværsstatistikkVirksomhetUtenVarighet>,
        årstall: Int,
        kvartal: Int,
        antallPersoner: Int,
        orgnr: String,
        tapteDagsverk: Int,
        muligeDagsverk: Int
    ) {
        val statistikkForVirksomhet = actual.stream()
            .filter { (årstall1, kvartal1, orgnr1): SykefraværsstatistikkVirksomhetUtenVarighet -> orgnr1 == orgnr && årstall1 == årstall && kvartal1 == kvartal }
            .toList()
        org.assertj.core.api.Assertions.assertThat(statistikkForVirksomhet.size).isEqualTo(1)
        assertSykefraværsstatistikkIsEqual(
            statistikkForVirksomhet[0],
            årstall,
            kvartal,
            antallPersoner,
            tapteDagsverk,
            muligeDagsverk
        )
    }

    private fun assertSykefraværsstatistikkForNæringIsEqual(
        actual: List<SykefraværsstatistikkForNæring>,
        årstall: Int,
        kvartal: Int,
        antallPersoner: Int,
        næring: Næring,
        tapteDagsverk: Int,
        muligeDagsverk: Int
    ) {
        val statistikkForNæring = actual.stream()
            .filter { (_, _, næringkode): SykefraværsstatistikkForNæring -> næringkode == næring.tosifferIdentifikator }
            .toList()
        org.assertj.core.api.Assertions.assertThat(statistikkForNæring.size).isEqualTo(1)
        assertSykefraværsstatistikkIsEqual(
            statistikkForNæring[0],
            årstall,
            kvartal,
            antallPersoner,
            tapteDagsverk,
            muligeDagsverk
        )
    }

    private fun assertSykefraværsstatistikkForBedreNæringskodeContains(
        actual: List<SykefraværsstatistikkForNæringskode>,
        årstall: Int,
        kvartal: Int,
        antallPersoner: Int,
        næringskode: Næringskode,
        tapteDagsverk: Int,
        muligeDagsverk: Int
    ) {
        val statistikkForNæring5Siffer = actual.stream()
            .filter { (årstall1, kvartal1, næringkode5siffer): SykefraværsstatistikkForNæringskode -> næringkode5siffer == næringskode.femsifferIdentifikator && årstall1 == årstall && kvartal1 == kvartal }
            .collect(Collectors.toList())
        org.assertj.core.api.Assertions.assertThat(statistikkForNæring5Siffer.size).isEqualTo(1)
        assertSykefraværsstatistikkIsEqual(
            statistikkForNæring5Siffer[0],
            årstall,
            kvartal,
            antallPersoner,
            tapteDagsverk,
            muligeDagsverk
        )
    }

    private fun assertSykefraværsstatistikkIsEqual(
        actual: Sykefraværsstatistikk,
        årstall: Int,
        kvartal: Int,
        antallPersoner: Int,
        tapteDagsverk: Int,
        muligeDagsverk: Int
    ) {
        org.assertj.core.api.Assertions.assertThat(actual.årstall).isEqualTo(årstall)
        org.assertj.core.api.Assertions.assertThat(actual.kvartal).isEqualTo(kvartal)
        org.assertj.core.api.Assertions.assertThat(actual.antallPersoner).isEqualTo(antallPersoner)
        assertBigDecimalIsEqual(actual.tapteDagsverk, BigDecimal(tapteDagsverk))
        assertBigDecimalIsEqual(actual.muligeDagsverk, BigDecimal(muligeDagsverk))
    }

    private fun opprettStatistikkLandTestData() {
        sykefraværStatistikkLandRepository.settInn(
            listOf(
                SykefraværsstatistikkLand(
                    årstall = 2019,
                    kvartal = 2,
                    antallPersoner = 2500000,
                    tapteDagsverk = BigDecimal("256800.0"),
                    muligeDagsverk = BigDecimal("60000000.0")
                )
            )
        )
        sykefraværStatistikkLandRepository.settInn(
            listOf(
                SykefraværsstatistikkLand(
                    årstall = 2019,
                    kvartal = 1,
                    antallPersoner = 2750000,
                    tapteDagsverk = BigDecimal("350000.0"),
                    muligeDagsverk = BigDecimal("71000000.0")
                )
            )
        )
    }

    private fun opprettStatistikkSektorTestData() {
        sykefraværStatistikkSektorRepository.settInn(
            listOf(
                SykefraværsstatistikkSektor(
                    sektorkode = Sektor.KOMMUNAL.sektorkode,
                    årstall = 2019,
                    kvartal = 2,
                    antallPersoner = 3,
                    tapteDagsverk = 1.toBigDecimal(),
                    muligeDagsverk = 60.toBigDecimal()
                ),
                SykefraværsstatistikkSektor(
                    sektorkode = Sektor.KOMMUNAL.sektorkode,
                    årstall = 2019,
                    kvartal = 1,
                    antallPersoner = 40,
                    tapteDagsverk = 20.toBigDecimal(),
                    muligeDagsverk = 115.toBigDecimal()
                ),
                SykefraværsstatistikkSektor(
                    sektorkode = Sektor.PRIVAT.sektorkode,
                    årstall = 2019,
                    kvartal = 2,
                    antallPersoner = 4,
                    tapteDagsverk = 9.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
                SykefraværsstatistikkSektor(
                    sektorkode = Sektor.PRIVAT.sektorkode,
                    årstall = 2019,
                    kvartal = 1,
                    antallPersoner = 7,
                    tapteDagsverk = 12.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
            )
        )
    }

    private fun opprettStatistikkVirksomhetTestData() {

        sykefraværsstatistikkVirksomhetRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhet(
                    orgnr = VIRKSOMHET_1,
                    årstall = 2018,
                    kvartal = 2,
                    antallPersoner = 3,
                    tapteDagsverk = BigDecimal("1.0"),
                    muligeDagsverk = BigDecimal("60.0"),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
                SykefraværsstatistikkVirksomhet(
                    orgnr = VIRKSOMHET_2,
                    årstall = 2018,
                    kvartal = 2,
                    antallPersoner = 4,
                    tapteDagsverk = BigDecimal("9.0"),
                    muligeDagsverk = BigDecimal("100.0"),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
                SykefraværsstatistikkVirksomhet(
                    orgnr = VIRKSOMHET_1,
                    årstall = 2018,
                    kvartal = 3,
                    antallPersoner = 3,
                    tapteDagsverk = BigDecimal("1.0"),
                    muligeDagsverk = BigDecimal("60.0"),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
                SykefraværsstatistikkVirksomhet(
                    orgnr = VIRKSOMHET_1,
                    årstall = 2018,
                    kvartal = 4,
                    antallPersoner = 40,
                    tapteDagsverk = BigDecimal("20.0"),
                    muligeDagsverk = BigDecimal("115.0"),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
                SykefraværsstatistikkVirksomhet(
                    orgnr = VIRKSOMHET_2,
                    årstall = 2018,
                    kvartal = 3,
                    antallPersoner = 4,
                    tapteDagsverk = BigDecimal("9.0"),
                    muligeDagsverk = BigDecimal("100.0"),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
                SykefraværsstatistikkVirksomhet(
                    orgnr = VIRKSOMHET_2,
                    årstall = 2018,
                    kvartal = 4,
                    antallPersoner = 7,
                    tapteDagsverk = BigDecimal("12.0"),
                    muligeDagsverk = BigDecimal("100.0"),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
                SykefraværsstatistikkVirksomhet(
                    orgnr = VIRKSOMHET_1,
                    årstall = 2019,
                    kvartal = 2,
                    antallPersoner = 3,
                    tapteDagsverk = BigDecimal("1.0"),
                    muligeDagsverk = BigDecimal("60.0"),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
                SykefraværsstatistikkVirksomhet(
                    orgnr = VIRKSOMHET_1,
                    årstall = 2019,
                    kvartal = 1,
                    antallPersoner = 40,
                    tapteDagsverk = BigDecimal("20.0"),
                    muligeDagsverk = BigDecimal("115.0"),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
                SykefraværsstatistikkVirksomhet(
                    orgnr = VIRKSOMHET_2,
                    årstall = 2019,
                    kvartal = 2,
                    antallPersoner = 4,
                    tapteDagsverk = BigDecimal("9.0"),
                    muligeDagsverk = BigDecimal("100.0"),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
                SykefraværsstatistikkVirksomhet(
                    orgnr = VIRKSOMHET_2,
                    årstall = 2019,
                    kvartal = 1,
                    antallPersoner = 7,
                    tapteDagsverk = BigDecimal("12.0"),
                    muligeDagsverk = BigDecimal("100.0"),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
            )
        )
    }

    private fun opprettStatistikkNæringTestData(vararg årstallOgKvartal: ÅrstallOgKvartal) {
        årstallOgKvartal.forEach {
            sykefraværStatistikkNæringRepository.settInn(listOf(
                SykefraværsstatistikkForNæring(
                    årstall = it.årstall,
                    kvartal = it.kvartal,
                    næringkode = produksjon.tosifferIdentifikator,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal("2.0"),
                    muligeDagsverk = BigDecimal("100.0")
                ),
                SykefraværsstatistikkForNæring(
                    årstall = it.årstall,
                    kvartal = it.kvartal,
                    næringkode = utdanning.tosifferIdentifikator,
                    antallPersoner = 8,
                    tapteDagsverk = BigDecimal("5.0"),
                    muligeDagsverk = BigDecimal("100.0")
                )
            ))
        }
    }

    private fun opprettStatistikkNæring5SifferTestData(vararg årstallOgKvartals: ÅrstallOgKvartal) {
        Arrays.stream(årstallOgKvartals)
            .forEach { (årstall, kvartal): ÅrstallOgKvartal ->
                createStatistikkNæring5Siffer(
                    produksjonAvKlær, årstall, kvartal, 10, 3, 100
                )
                createStatistikkNæring5Siffer(
                    undervisning, årstall, kvartal, 10, 5, 100
                )
            }
    }

    private fun createStatistikkNæring5Siffer(
        næringskode: Næringskode,
        årstall: Int,
        kvartal: Int,
        antallPersoner: Int,
        tapteDagsverk: Int,
        muligeDagsverk: Int
    ) {
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring5siffer "
                    + "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "values (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(
                SykefraværsstatistikkForNæringskode(
                    årstall,
                    kvartal,
                    næringskode.femsifferIdentifikator,
                    antallPersoner,
                    BigDecimal(tapteDagsverk),
                    BigDecimal(muligeDagsverk)
                )
            )
        )
    }

    private fun parametre(
        sykefraværsstatistikkForNæringskode: SykefraværsstatistikkForNæringskode
    ): MapSqlParameterSource {
        val parametre = MapSqlParameterSource()
            .addValue("naring_kode", sykefraværsstatistikkForNæringskode.næringkode5siffer)
        return leggTilParametreForSykefraværsstatistikk(parametre, sykefraværsstatistikkForNæringskode)
    }

    private fun leggTilParametreForSykefraværsstatistikk(
        parametre: MapSqlParameterSource, sykefraværsstatistikk: Sykefraværsstatistikk
    ): MapSqlParameterSource {
        return parametre
            .addValue("arstall", sykefraværsstatistikk.årstall)
            .addValue("kvartal", sykefraværsstatistikk.kvartal)
            .addValue("antall_personer", sykefraværsstatistikk.antallPersoner)
            .addValue("tapte_dagsverk", sykefraværsstatistikk.tapteDagsverk)
            .addValue("mulige_dagsverk", sykefraværsstatistikk.muligeDagsverk)
    }
}
