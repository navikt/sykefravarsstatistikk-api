package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import ia.felles.definisjoner.bransjer.BransjeId
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import org.assertj.core.api.AssertionsForClassTypes
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
import ia.felles.definisjoner.bransjer.Bransje
import org.jetbrains.exposed.sql.deleteAll

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class SykefraværForEttKvartalRepositoryJdbcTest {

    @Autowired
    private lateinit var sykefraværStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository

    @Autowired
    private lateinit var sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository

    @Autowired
    private lateinit var sykefraværStatistikkSektorRepository: SykefraværStatistikkSektorRepository

    @Autowired
    private lateinit var sykefraværStatistikkNæringRepository: SykefraværStatistikkNæringRepository

    @Autowired
    private lateinit var sykefraværStatistikkNæringskodeRepository: SykefraværStatistikkNæringskodeRepository

    val SISTE_PUBLISERTE_KVARTAL = ÅrstallOgKvartal(2022, 1)

    @BeforeEach
    fun setUp() {
        with(sykefraværStatistikkVirksomhetRepository) { transaction { deleteAll() } }
        with(sykefraværStatistikkLandRepository) { transaction { deleteAll() } }
    }

    @AfterEach
    fun tearDown() {
        with(sykefraværStatistikkVirksomhetRepository) { transaction { deleteAll() } }
        with(sykefraværStatistikkLandRepository) { transaction { deleteAll() } }
    }

    @Test
    fun hentSykefraværprosentLand__skal_returnere_riktig_sykefravær() {
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
        val resultat = sykefraværStatistikkLandRepository.hentAlt()
        AssertionsForClassTypes.assertThat(resultat.size).isEqualTo(3)
        AssertionsForClassTypes.assertThat(resultat[0])
            .isEqualTo(
                SykefraværForEttKvartal(
                    SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2),
                    BigDecimal(6),
                    BigDecimal(100),
                    10
                )
            )
    }

    @Test
    fun hentSykefraværprosentSektor__skal_returnere_riktig_sykefravær() {

        sykefraværStatistikkSektorRepository.settInn(
            listOf(
                SykefraværsstatistikkSektor(
                    årstall = 2019,
                    kvartal = 2,
                    sektor = Sektor.STATLIG.sektorkode,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(2),
                    muligeDagsverk = BigDecimal(100)
                ),
                SykefraværsstatistikkSektor(
                    årstall = 2019,
                    kvartal = 1,
                    sektor = Sektor.STATLIG.sektorkode,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(2),
                    muligeDagsverk = BigDecimal(100)
                ),
                SykefraværsstatistikkSektor(
                    årstall = 2018,
                    kvartal = 4,
                    sektor = Sektor.STATLIG.sektorkode,
                    antallPersoner = 10,
                    tapteDagsverk = 4.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
                SykefraværsstatistikkSektor(
                    årstall = 2018,
                    kvartal = 4,
                    sektor = Sektor.KOMMUNAL.sektorkode,
                    antallPersoner = 10,
                    tapteDagsverk = 5.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),

                )
        )

        val resultat =
            sykefraværStatistikkSektorRepository.hentKvartalsvisSykefraværprosent(
                Sektor.STATLIG
            )
        AssertionsForClassTypes.assertThat(resultat.size).isEqualTo(3)
        AssertionsForClassTypes.assertThat(resultat[0])
            .isEqualTo(
                SykefraværForEttKvartal(
                    ÅrstallOgKvartal(2018, 4), BigDecimal(4), BigDecimal(100), 10
                )
            )
    }

    @Test
    fun hentSykefraværprosentNæring__skal_returnere_riktig_sykefravær() {
        val produksjonAvKlær = Næring("14")
        sykefraværStatistikkNæringRepository.settInn(
            listOf(
                SykefraværsstatistikkForNæring(
                    næring = produksjonAvKlær.tosifferIdentifikator,
                    årstall = 2019,
                    kvartal = 2,
                    antallPersoner = 10,
                    tapteDagsverk = 2.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
                SykefraværsstatistikkForNæring(
                    næring = produksjonAvKlær.tosifferIdentifikator,
                    årstall = 2019,
                    kvartal = 1,
                    antallPersoner = 10,
                    tapteDagsverk = 3.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
                SykefraværsstatistikkForNæring(
                    næring = Næring("85").tosifferIdentifikator,
                    årstall = 2018,
                    kvartal = 4,
                    antallPersoner = 10,
                    tapteDagsverk = 5.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
            )
        )
        val resultat = sykefraværStatistikkNæringRepository.hentKvartalsvisSykefraværprosent(produksjonAvKlær)
        AssertionsForClassTypes.assertThat(resultat.size).isEqualTo(2)
        AssertionsForClassTypes.assertThat(resultat[0])
            .isEqualTo(
                SykefraværForEttKvartal(
                    ÅrstallOgKvartal(2019, 1), BigDecimal(3), BigDecimal(100), 10
                )
            )
    }

    @Test
    fun `hentKvartalsvisSykefraværprosent skal returnere riktig sykefravær`() {
        sykefraværStatistikkNæringskodeRepository.settInn(
            listOf(
                SykefraværsstatistikkForNæringskode(
                    næringskode = Næringskode("87101").femsifferIdentifikator,
                    årstall = 2019,
                    kvartal = 2,
                    antallPersoner = 10,
                    tapteDagsverk = 5.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
                SykefraværsstatistikkForNæringskode(
                    næringskode = Næringskode("87101").femsifferIdentifikator,
                    årstall = 2019,
                    kvartal = 1,
                    antallPersoner = 10,
                    tapteDagsverk = 1.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
                SykefraværsstatistikkForNæringskode(
                    næringskode = Næringskode("87102").femsifferIdentifikator,
                    årstall = 2019,
                    kvartal = 1,
                    antallPersoner = 10,
                    tapteDagsverk = 7.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
                SykefraværsstatistikkForNæringskode(
                    næringskode = Næringskode("87301").femsifferIdentifikator,
                    årstall = 2018,
                    kvartal = 4,
                    antallPersoner = 10,
                    tapteDagsverk = 6.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
            )
        )

        val resultat =
            sykefraværStatistikkNæringskodeRepository.hentKvartalsvisSykefraværprosent(
                (Bransje.SYKEHJEM.bransjeId as BransjeId.Næringskoder).næringskoder.map { Næringskode(it) },
            )
        AssertionsForClassTypes.assertThat(resultat.size).isEqualTo(2)
        AssertionsForClassTypes.assertThat(resultat[0])
            .isEqualTo(
                SykefraværForEttKvartal(
                    ÅrstallOgKvartal(2019, 1), BigDecimal(8), BigDecimal(200), 20
                )
            )
    }

    @Test
    fun hentSykefraværprosentVirksomhet__skal_returnere_riktig_sykefravær() {
        val barnehage = Underenhet.Næringsdrivende(
            Orgnr("999999999"),
            Orgnr("1111111111"),
            "test Barnehage",
            Næringskode("88911"),
            10
        )

        sykefraværStatistikkVirksomhetRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhet(
                    årstall = 2019,
                    kvartal = 2,
                    orgnr = barnehage.orgnr.verdi,
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                    rectype = "2",
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(2),
                    muligeDagsverk = BigDecimal(100)
                ),
                SykefraværsstatistikkVirksomhet(
                    årstall = 2019,
                    kvartal = 1,
                    orgnr = "987654321",
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                    rectype = "2",
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(3),
                    muligeDagsverk = BigDecimal(100)
                ),
                SykefraværsstatistikkVirksomhet(
                    årstall = 2018,
                    kvartal = 4,
                    antallPersoner = 10,
                    tapteDagsverk = 5.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal(),
                    orgnr = barnehage.orgnr.verdi,
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                    rectype = "2"
                )
            )
        )

        val resultat = sykefraværStatistikkVirksomhetRepository.hentAlt(barnehage.orgnr)

        AssertionsForClassTypes.assertThat(resultat.size).isEqualTo(2)
        AssertionsForClassTypes.assertThat(resultat[0])
            .isEqualTo(
                SykefraværForEttKvartal(
                    ÅrstallOgKvartal(2018, 4), BigDecimal(5), BigDecimal(100), 10
                )
            )
    }

    @Test
    fun hentSykefraværprosentVirksomhet__skal_summere_sykefravær_på_varighet() {
        val barnehage = Underenhet.Næringsdrivende(
            Orgnr("999999999"),
            Orgnr("1111111111"),
            "test Barnehage",
            Næringskode("88911"),
            10
        )

        sykefraværStatistikkVirksomhetRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhet(
                    årstall = 2019,
                    kvartal = 2,
                    orgnr = barnehage.orgnr.verdi,
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                    rectype = "2",
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(2),
                    muligeDagsverk = BigDecimal(100)
                ),
                SykefraværsstatistikkVirksomhet(
                    årstall = 2019,
                    kvartal = 2,
                    antallPersoner = 10,
                    tapteDagsverk = 5.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal(),
                    orgnr = barnehage.orgnr.verdi,
                    varighet = Varighetskategori._8_UKER_TIL_20_UKER.kode,
                    rectype = "2"
                )
            )
        )

        val resultat = sykefraværStatistikkVirksomhetRepository.hentAlt(barnehage.orgnr)

        AssertionsForClassTypes.assertThat(resultat)
            .isEqualTo(
                listOf(
                    SykefraværForEttKvartal(
                        ÅrstallOgKvartal(2019, 2), BigDecimal(7), BigDecimal(200), 20
                    )
                )
            )
    }

}
