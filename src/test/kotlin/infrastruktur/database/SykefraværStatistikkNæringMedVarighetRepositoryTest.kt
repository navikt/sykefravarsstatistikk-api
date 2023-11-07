package infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.UmaskertSykefraværForEttKvartalMedVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkNæringMedVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværStatistikkNæringMedVarighetRepository
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
open class SykefraværStatistikkNæringMedVarighetRepositoryTest{

    @Autowired
    private lateinit var sykefraværStatistikkNæringMedVarighetRepository: SykefraværStatistikkNæringMedVarighetRepository

    @BeforeEach
    fun setUp() {
        TestUtils.slettAllStatistikkFraDatabase(
            sykefraværStatistikkNæringMedVarighetRepository = sykefraværStatistikkNæringMedVarighetRepository
        )
    }

    @Test
    fun `settInn skal lagre data i tabellen`() {
        sykefraværStatistikkNæringMedVarighetRepository.settInn(
            listOf(
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2019,
                    kvartal = 1,
                    næringkode = "03123",
                    varighet = "A",
                    antallPersoner = 14,
                    tapteDagsverk = BigDecimal("55.123"),
                    muligeDagsverk = BigDecimal("856.891")
                )
            )
        )
        val resultat = sykefraværStatistikkNæringMedVarighetRepository.hentAlt()

        resultat.size shouldBe 1
        resultat[0] shouldBeEqual
                UmaskertSykefraværForEttKvartalMedVarighet(
                    årstallOgKvartal = ÅrstallOgKvartal(2019, 1),
                    tapteDagsverk = BigDecimal("55.123"),
                    muligeDagsverk = BigDecimal("856.891"),
                    antallPersoner = 14,
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER
                )
    }

    @Test
    fun `slettKvartal skal slette data for riktig kvartal`() {
        sykefraværStatistikkNæringMedVarighetRepository.settInn(
            listOf(
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2018,
                    kvartal = 4,
                    tapteDagsverk = 30.toBigDecimal(),
                    muligeDagsverk = 300.toBigDecimal(),
                    antallPersoner = 15,
                    varighet = "A",
                    næringkode = "02000"
                ),
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2019,
                    kvartal = 1,
                    tapteDagsverk = 30.toBigDecimal(),
                    muligeDagsverk = 300.toBigDecimal(),
                    antallPersoner = 15,
                    varighet = "A",
                    næringkode = "01000"
                ),
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2020,
                    kvartal = 1,
                    tapteDagsverk = 30.toBigDecimal(),
                    muligeDagsverk = 300.toBigDecimal(),
                    antallPersoner = 15,
                    varighet = "A",
                    næringkode = "01000"
                ),
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2019,
                    kvartal = 1,
                    tapteDagsverk = 30.toBigDecimal(),
                    muligeDagsverk = 300.toBigDecimal(),
                    antallPersoner = 15,
                    varighet = "A",
                    næringkode = "02000"
                )
            )
        )

        val antallSlettet = sykefraværStatistikkNæringMedVarighetRepository.slettKvartal(ÅrstallOgKvartal(2019, 1))
        val antallGjenværende = sykefraværStatistikkNæringMedVarighetRepository.hentAlt().size

        antallSlettet shouldBe 2
        antallGjenværende shouldBe 2
    }



    private fun SykefraværStatistikkNæringMedVarighetRepository.hentAlt(): List<UmaskertSykefraværForEttKvartalMedVarighet> {
        return transaction {
            selectAll().map {
                UmaskertSykefraværForEttKvartalMedVarighet(
                    årstallOgKvartal = ÅrstallOgKvartal(it[årstall], it[kvartal]),
                    tapteDagsverk = it[tapteDagsverk].toBigDecimal(),
                    muligeDagsverk = it[muligeDagsverk].toBigDecimal(),
                    antallPersoner = it[antallPersoner],
                    varighet = Varighetskategori.fraKode(it[varighet].toString())
                )
            }
        }
    }
}