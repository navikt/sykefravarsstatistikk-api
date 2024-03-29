package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import ia.felles.definisjoner.bransjer.Bransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import org.assertj.core.api.Assertions
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
open class SykefraværRepositoryJdbcTest {

    @Autowired
    private lateinit var sykefravarStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository

    @Autowired
    private lateinit var sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository

    @Autowired
    private lateinit var sykefraværStatistikkNæringRepository: SykefraværStatistikkNæringRepository

    @Autowired
    private lateinit var sykefraværStatistikkNæringskodeRepository: SykefraværStatistikkNæringskodeRepository

    val SISTE_PUBLISERTE_KVARTAL = ÅrstallOgKvartal(2022, 1)

    @BeforeEach
    fun setUp() {
        with(sykefravarStatistikkVirksomhetRepository) { transaction { deleteAll() } }
        with(sykefraværStatistikkLandRepository) { transaction { deleteAll() } }
    }

    @AfterEach
    fun tearDown() {
        with(sykefravarStatistikkVirksomhetRepository) { transaction { deleteAll() } }
        with(sykefraværStatistikkLandRepository) { transaction { deleteAll() } }
    }

    @Test
    fun `hentUmaskertSykefraværForNorge skal hente riktig data`() {

        sykefraværStatistikkLandRepository.settInn(
            listOf(
                SykefraværsstatistikkLand(
                    årstall = this.SISTE_PUBLISERTE_KVARTAL.årstall,
                    kvartal = this.SISTE_PUBLISERTE_KVARTAL.kvartal,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal("4.0"),
                    muligeDagsverk = BigDecimal("100.0")
                ),
                SykefraværsstatistikkLand(
                    årstall = this.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).årstall,
                    kvartal = this.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).kvartal,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal("5.0"),
                    muligeDagsverk = BigDecimal("100.0")
                ),
                SykefraværsstatistikkLand(
                    årstall = this.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).årstall,
                    kvartal = this.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).kvartal,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal("6.0"),
                    muligeDagsverk = BigDecimal("100.0")
                ),
            )
        )
        val kvartaler = SISTE_PUBLISERTE_KVARTAL inkludertTidligere 1
        val resultat = sykefraværStatistikkLandRepository.hentForKvartaler(kvartaler)

        Assertions.assertThat(resultat.size).isEqualTo(2)
        Assertions.assertThat(resultat)
            .containsExactlyInAnyOrderElementsOf(
                listOf(
                    sykefraværForEtÅrstallOgKvartal(
                        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).årstall,
                        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).kvartal,
                        5
                    ),
                    sykefraværForEtÅrstallOgKvartal(
                        SISTE_PUBLISERTE_KVARTAL.årstall,
                        SISTE_PUBLISERTE_KVARTAL.kvartal,
                        4
                    )
                )
            )
    }

    @Test
    fun hentSykefraværprosentVirksomhet__skal_returnerer_empty_list_dersom_ingen_data_funnet_for_årstall_og_kvartal() {
        persisterDatasetIDb(BARNEHAGE)
        val resultat =
            sykefravarStatistikkVirksomhetRepository.hentUmaskertSykefravær(
                BARNEHAGE,
                listOf(ÅrstallOgKvartal(2021, 4))
            )
        Assertions.assertThat(resultat.size).isEqualTo(0)
    }

    @Test
    fun hentSykefraværprosentVirksomhet__skal_returnere_riktig_sykefravær() {
        persisterDatasetIDb(BARNEHAGE)
        val resultat =
            sykefravarStatistikkVirksomhetRepository.hentUmaskertSykefravær(
                virksomhet = BARNEHAGE,
                kvartaler = ÅrstallOgKvartal(2019, 2) inkludertTidligere 3
            )
        Assertions.assertThat(resultat.size).isEqualTo(4)
        Assertions.assertThat(resultat[0]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2018, 3, 6))
        Assertions.assertThat(resultat[3]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 2, 2))
    }

    @Test
    fun hentSykefraværprosentVirksomhet__skal_returnere_riktig_sykefravær_for_ønskede_kvartaler() {
        persisterDatasetIDb(BARNEHAGE)
        val resultat =
            sykefravarStatistikkVirksomhetRepository.hentUmaskertSykefravær(
                BARNEHAGE,
                ÅrstallOgKvartal(2019, 2) inkludertTidligere 1
            )
        Assertions.assertThat(resultat.size).isEqualTo(2)
        Assertions.assertThat(resultat[0]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 1, 3))
        Assertions.assertThat(resultat[1]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 2, 2))
    }

    @Test
    fun hentUmaskertSykefraværForEttKvartalListe_skal_hente_riktig_data() {
        persisterDatasetIDb(Næring("10"))
        val resultat = sykefraværStatistikkNæringRepository.hentForKvartaler(
            Næring("10"),
            ÅrstallOgKvartal(2019, 2) inkludertTidligere 1
        )
        Assertions.assertThat(resultat.size).isEqualTo(2)
        Assertions.assertThat(resultat[0]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 1, 3))
        Assertions.assertThat(resultat[1]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 2, 2))
    }

    @Test
    fun `skal hente riktig data for næringskode`() {
        sykefraværStatistikkNæringskodeRepository.settInn(
            listOf(
                SykefraværsstatistikkForNæringskode(
                    årstall = 2019,
                    kvartal = 2,
                    næringskode = Næringskode("88911").femsifferIdentifikator,
                    tapteDagsverk = 1.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
                SykefraværsstatistikkForNæringskode(
                    årstall = 2019,
                    kvartal = 1,
                    næringskode = Næringskode("88911").femsifferIdentifikator,
                    tapteDagsverk = 2.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
                SykefraværsstatistikkForNæringskode(
                    årstall = 2019,
                    kvartal = 1,
                    næringskode = Næringskode("99999").femsifferIdentifikator,
                    tapteDagsverk = 3.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
                SykefraværsstatistikkForNæringskode(
                    årstall = 2018,
                    kvartal = 4,
                    næringskode = Næringskode("88911").femsifferIdentifikator,
                    tapteDagsverk = 4.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
            )
        )
        val resultat = sykefraværStatistikkNæringskodeRepository.hentForBransje(
            Bransje.BARNEHAGER,
            listOf(
                ÅrstallOgKvartal(2019, 1),
                ÅrstallOgKvartal(2018, 4)
            )
        )
        Assertions.assertThat(resultat.size).isEqualTo(2)
        Assertions.assertThat(resultat[0].dagsverkTeller).isEqualTo(BigDecimal("4.0"))
        Assertions.assertThat(resultat[1].dagsverkNevner).isEqualTo(BigDecimal("100.0"))
    }

    private fun persisterDatasetIDb(barnehage: Underenhet.Næringsdrivende) {

        sykefravarStatistikkVirksomhetRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhet(
                    orgnr = barnehage.orgnr.verdi,
                    årstall = 2019,
                    kvartal = 2,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(2),
                    muligeDagsverk = BigDecimal(100),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
                SykefraværsstatistikkVirksomhet(
                    orgnr = "987654321",
                    årstall = 2019,
                    kvartal = 1,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(3),
                    muligeDagsverk = BigDecimal(100),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
                SykefraværsstatistikkVirksomhet(
                    orgnr = barnehage.orgnr.verdi,
                    årstall = 2019,
                    kvartal = 1,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(3),
                    muligeDagsverk = BigDecimal(100),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
                SykefraværsstatistikkVirksomhet(
                    orgnr = barnehage.orgnr.verdi,
                    årstall = 2018,
                    kvartal = 4,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(5),
                    muligeDagsverk = BigDecimal(100),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
                SykefraværsstatistikkVirksomhet(
                    orgnr = barnehage.orgnr.verdi,
                    årstall = 2018,
                    kvartal = 3,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(6),
                    muligeDagsverk = BigDecimal(100),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
            )
        )
    }

    private fun persisterDatasetIDb(næring: Næring) {
        sykefraværStatistikkNæringRepository.settInn(
            listOf(
                SykefraværsstatistikkForNæring(
                    næring = næring.tosifferIdentifikator,
                    årstall = 2019,
                    kvartal = 2,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(2),
                    muligeDagsverk = BigDecimal(100)
                ),
                SykefraværsstatistikkForNæring(
                    næring = "94",
                    årstall = 2019,
                    kvartal = 1,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(3),
                    muligeDagsverk = BigDecimal(100)
                ),
                SykefraværsstatistikkForNæring(
                    næring = næring.tosifferIdentifikator,
                    årstall = 2019,
                    kvartal = 1,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(3),
                    muligeDagsverk = BigDecimal(100)
                ),
                SykefraværsstatistikkForNæring(
                    næring = næring.tosifferIdentifikator,
                    årstall = 2018,
                    kvartal = 4,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(5),
                    muligeDagsverk = BigDecimal(100)
                ),
                SykefraværsstatistikkForNæring(
                    næring = næring.tosifferIdentifikator,
                    årstall = 2018,
                    kvartal = 3,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(6),
                    muligeDagsverk = BigDecimal(100)
                ),
            )
        )
    }

    companion object {
        val BARNEHAGE = Underenhet.Næringsdrivende(
            Orgnr("999999999"),
            Orgnr("1111111111"),
            "test Barnehage",
            Næringskode("88911"),
            10
        )

        private fun sykefraværForEtÅrstallOgKvartal(
            årstall: Int, kvartal: Int, totalTapteDagsverk: Int
        ): UmaskertSykefraværForEttKvartal {
            return UmaskertSykefraværForEttKvartal(
                ÅrstallOgKvartal(årstall, kvartal),
                BigDecimal(totalTapteDagsverk),
                BigDecimal(100),
                10
            )
        }
    }
}
