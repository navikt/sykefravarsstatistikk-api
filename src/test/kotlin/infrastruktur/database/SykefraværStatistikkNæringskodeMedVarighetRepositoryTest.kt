package infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkNæringMedVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværStatistikkNæringskodeMedVarighetRepository
import org.jetbrains.exposed.sql.selectAll
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
open class SykefraværStatistikkNæringskodeMedVarighetRepositoryTest{

    @Autowired
    private lateinit var sykefraværStatistikkNæringskodeMedVarighetRepository: SykefraværStatistikkNæringskodeMedVarighetRepository

    @BeforeEach
    fun setUp() {
        TestUtils.slettAllStatistikkFraDatabase(
            sykefraværStatistikkNæringskodeMedVarighetRepository = sykefraværStatistikkNæringskodeMedVarighetRepository
        )
    }

    @Test
    fun `settInn skal lagre data i tabellen`() {
        sykefraværStatistikkNæringskodeMedVarighetRepository.settInn(
            listOf(
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2019,
                    kvartal = 1,
                    næringkode = "03123",
                    varighet = 'A',
                    antallPersoner = 14,
                    tapteDagsverk = BigDecimal("55.123"),
                    muligeDagsverk = BigDecimal("856.891")
                )
            )
        )
        val resultat = sykefraværStatistikkNæringskodeMedVarighetRepository.hentAlt()

        resultat.size shouldBe 1
        resultat[0] shouldBeEqual
                UmaskertSykefraværForEttKvartal(
                    årstallOgKvartal = ÅrstallOgKvartal(2019, 1),
                    dagsverkTeller = BigDecimal("55.123"),
                    dagsverkNevner = BigDecimal("856.891"),
                    antallPersoner = 14,
                )
    }

    @Test
    fun `slettKvartal skal slette data for riktig kvartal`() {
        sykefraværStatistikkNæringskodeMedVarighetRepository.settInn(
            listOf(
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2018,
                    kvartal = 4,
                    tapteDagsverk = 30.toBigDecimal(),
                    muligeDagsverk = 300.toBigDecimal(),
                    antallPersoner = 15,
                    varighet = 'A',
                    næringkode = "02000"
                ),
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2019,
                    kvartal = 1,
                    tapteDagsverk = 30.toBigDecimal(),
                    muligeDagsverk = 300.toBigDecimal(),
                    antallPersoner = 15,
                    varighet = 'A',
                    næringkode = "01000"
                ),
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2020,
                    kvartal = 1,
                    tapteDagsverk = 30.toBigDecimal(),
                    muligeDagsverk = 300.toBigDecimal(),
                    antallPersoner = 15,
                    varighet = 'A',
                    næringkode = "01000"
                ),
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2019,
                    kvartal = 1,
                    tapteDagsverk = 30.toBigDecimal(),
                    muligeDagsverk = 300.toBigDecimal(),
                    antallPersoner = 15,
                    varighet = 'A',
                    næringkode = "02000"
                )
            )
        )

        val antallSlettet = sykefraværStatistikkNæringskodeMedVarighetRepository.slettKvartal(ÅrstallOgKvartal(2019, 1))
        val antallGjenværende = sykefraværStatistikkNæringskodeMedVarighetRepository.hentAlt().size

        antallSlettet shouldBe 2
        antallGjenværende shouldBe 2
    }



    private fun SykefraværStatistikkNæringskodeMedVarighetRepository.hentAlt(): List<UmaskertSykefraværForEttKvartal> {
        return transaction {
            selectAll().map {
                UmaskertSykefraværForEttKvartal(
                    årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal]),
                    dagsverkTeller = it[tapteDagsverk].toBigDecimal(),
                    dagsverkNevner = it[muligeDagsverk].toBigDecimal(),
                    antallPersoner = it[antallPersoner],
                )
            }
        }
    }
}