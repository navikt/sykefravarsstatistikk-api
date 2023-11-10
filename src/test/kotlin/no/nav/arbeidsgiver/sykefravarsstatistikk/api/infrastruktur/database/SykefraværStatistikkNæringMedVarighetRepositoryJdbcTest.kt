package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import ia.felles.definisjoner.bransjer.Bransje
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import org.assertj.core.api.Assertions.assertThat
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
import java.math.BigDecimal

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class SykefraværStatistikkNæringMedVarighetRepositoryJdbcTest {

    @Autowired
    private lateinit var sykefraværStatistikkNæringMedVarighetRepository: SykefraværStatistikkNæringMedVarighetRepository

    @Autowired
    private lateinit var sykefravarStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository

    @BeforeEach
    fun setUp() {
        TestUtils.slettAllStatistikkFraDatabase(
            sykefravarStatistikkVirksomhetRepository = sykefravarStatistikkVirksomhetRepository,
            sykefraværStatistikkNæringMedVarighetRepository = sykefraværStatistikkNæringMedVarighetRepository,
        )
    }

    @Test
    fun hentSykefraværForEttKvartalMedVarighet__skal_returnere_riktig_sykefravær() {
        val barnehage = Underenhet.Næringsdrivende(
            orgnr = Orgnr("999999999"),
            overordnetEnhetOrgnr = Orgnr("1111111111"),
            navn = "test Barnehage",
            næringskode = Næringskode("88911"),
            antallAnsatte = 10
        )

        sykefravarStatistikkVirksomhetRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhet(
                    årstall = 2019,
                    kvartal = 2,
                    orgnr = barnehage.orgnr.verdi,
                    tapteDagsverk = BigDecimal(4),
                    muligeDagsverk = BigDecimal(0),
                    antallPersoner = 0,
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                    rectype = "2"
                ),
                SykefraværsstatistikkVirksomhet(
                    årstall = 2019,
                    kvartal = 2,
                    orgnr = barnehage.orgnr.verdi,
                    varighet = Varighetskategori.TOTAL.kode,
                    rectype = "2",
                    antallPersoner = 6,
                    tapteDagsverk = 0.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                )
            )
        )

        val resultat =
            sykefravarStatistikkVirksomhetRepository.hentKorttidsfravær(barnehage.orgnr)
        resultat.size shouldBe 2
        resultat[0] shouldBeEqual
                UmaskertSykefraværForEttKvartal(
                    årstallOgKvartal = ÅrstallOgKvartal(2019, 2),
                    dagsverkTeller = BigDecimal("4.0"),
                    dagsverkNevner = BigDecimal("0.0"),
                    antallPersoner = 0,
                )
        resultat[1] shouldBeEqual
                UmaskertSykefraværForEttKvartal(
                    årstallOgKvartal = ÅrstallOgKvartal(2019, 2),
                    dagsverkTeller = BigDecimal("0.0"),
                    dagsverkNevner = BigDecimal("100.0"),
                    antallPersoner = 6,
                )
    }

    @Test
    fun hentSykefraværForEttKvartalMedVarighet_for_næring__skal_returnere_riktig_sykefravær() {
        val barnehager = Næringskode(femsifferIdentifikator = "88911")
        val årstallOgKvartal = ÅrstallOgKvartal(årstall = 2019, kvartal = 2)
        sykefraværStatistikkNæringMedVarighetRepository.settInn(
            listOf(
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = årstallOgKvartal.årstall,
                    kvartal = årstallOgKvartal.kvartal,
                    næringkode = barnehager.femsifferIdentifikator,
                    varighet = Varighetskategori.TOTAL.kode,
                    antallPersoner = 1,
                    tapteDagsverk = 0.toBigDecimal(),
                    muligeDagsverk = 10.toBigDecimal()
                ),
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = årstallOgKvartal.årstall,
                    kvartal = årstallOgKvartal.kvartal,
                    næringkode = barnehager.femsifferIdentifikator,
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                    antallPersoner = 0,
                    tapteDagsverk = 4.toBigDecimal(),
                    muligeDagsverk = 0.toBigDecimal()
                )
            )
        )
        val resultat =
            sykefraværStatistikkNæringMedVarighetRepository.hentKorttidsfravær(barnehager.næring)
        resultat.size shouldBe 2
        resultat[0] shouldBeEqualToComparingFields
                UmaskertSykefraværForEttKvartal(
                    årstallOgKvartal = ÅrstallOgKvartal(2019, 2),
                    dagsverkTeller = BigDecimal("4.0"),
                    dagsverkNevner = BigDecimal("0.0"),
                    antallPersoner = 0,
                )

        resultat[1] shouldBeEqualToComparingFields
                UmaskertSykefraværForEttKvartal(
                    årstallOgKvartal = ÅrstallOgKvartal(2019, 2),
                    dagsverkTeller = BigDecimal("0.0"),
                    dagsverkNevner = BigDecimal("10.0"),
                    antallPersoner = 1,
                )
    }

    @Test
    fun hentSykefraværForEttKvartalMedVarighet_for_bransje__skal_returnere_riktig_sykefravær() {
        val sykehus = Næringskode("86101")
        val legetjeneste = Næringskode("86211")
        val årstallOgKvartal = ÅrstallOgKvartal(2019, 2)

        sykefraværStatistikkNæringMedVarighetRepository.settInn(
            listOf(
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = årstallOgKvartal.årstall,
                    kvartal = årstallOgKvartal.kvartal,
                    næringkode = sykehus.femsifferIdentifikator,
                    varighet = Varighetskategori.TOTAL.kode,
                    antallPersoner = 1,
                    tapteDagsverk = 0.toBigDecimal(),
                    muligeDagsverk = 10.toBigDecimal()
                ),
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = årstallOgKvartal.årstall,
                    kvartal = årstallOgKvartal.kvartal,
                    næringkode = sykehus.femsifferIdentifikator,
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                    antallPersoner = 0,
                    tapteDagsverk = 4.toBigDecimal(),
                    muligeDagsverk = 0.toBigDecimal()
                ),
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = årstallOgKvartal.årstall,
                    kvartal = årstallOgKvartal.kvartal,
                    næringkode = legetjeneste.femsifferIdentifikator,
                    varighet = Varighetskategori.TOTAL.kode,
                    antallPersoner = 5,
                    tapteDagsverk = 0.toBigDecimal(),
                    muligeDagsverk = 50.toBigDecimal()
                ),
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = årstallOgKvartal.årstall,
                    kvartal = årstallOgKvartal.kvartal,
                    næringkode = legetjeneste.femsifferIdentifikator,
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                    antallPersoner = 0,
                    tapteDagsverk = 8.toBigDecimal(),
                    muligeDagsverk = 0.toBigDecimal()
                ),
            )
        )
        val resultat =
            sykefraværStatistikkNæringMedVarighetRepository.hentKorttidsfravær(Bransje.SYKEHUS.bransjeId)

        resultat.size shouldBe 2

        resultat[0] shouldBeEqualToComparingFields
                UmaskertSykefraværForEttKvartal(
                    årstallOgKvartal = ÅrstallOgKvartal(2019, 2),
                    dagsverkTeller = BigDecimal("4.0"),
                    dagsverkNevner = BigDecimal("0.0"),
                    antallPersoner = 0,
                )

        resultat[1] shouldBeEqualToComparingFields
                UmaskertSykefraværForEttKvartal(
                    årstallOgKvartal = ÅrstallOgKvartal(2019, 2),
                    dagsverkTeller = BigDecimal("0.0"),
                    dagsverkNevner = BigDecimal("10.0"),
                    antallPersoner = 1,
                )

    }

    @Test
    fun `hent sykefravær med varighet for næring burde returnere sykefraværsstatistikk for alle inkluderte næringskoder`() {
        val næringskode1 = Næringskode("84300")
        val næringskode2 = Næringskode("84999")

        // Populer databasen med statistikk for to næringskoder, som har felles næring
        sykefraværStatistikkNæringMedVarighetRepository.settInn(
            listOf(
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2023,
                    kvartal = 1,
                    næringkode = næringskode1.femsifferIdentifikator,
                    varighet = 'E',
                    antallPersoner = 20,
                    tapteDagsverk = 100.toBigDecimal(),
                    muligeDagsverk = 1000.toBigDecimal()
                )
            )
        )
        sykefraværStatistikkNæringMedVarighetRepository.settInn(
            listOf(
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2023,
                    kvartal = 1,
                    næringkode = næringskode2.femsifferIdentifikator,
                    varighet = 'E',
                    antallPersoner = 20,
                    tapteDagsverk = 400.toBigDecimal(),
                    muligeDagsverk = 1000.toBigDecimal()
                )
            )
        )

        val resultat =
            sykefraværStatistikkNæringMedVarighetRepository.hentLangtidsfravær(næringskode1.næring)

        // Resultatet skal bli statistikk for BEGGE de to næringskodene
        assertThat(resultat.size).isEqualTo(2)
    }
}
