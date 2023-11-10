package infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværStatistikkNæringRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import testUtils.TestUtils.slettAllStatistikkFraDatabase
import java.math.BigDecimal


@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class SykefravarStatistikkNæringRepositoryTest {

    @Autowired
    private lateinit var sykefraværStatistikkNæringRepository: SykefraværStatistikkNæringRepository

    private val utdanning = Næring("86")
    private val produksjon = Næring("14")

    @AfterEach
    fun tearDown() {
        slettAllStatistikkFraDatabase(
            sykefraværStatistikkNæringRepository = sykefraværStatistikkNæringRepository,
        )
    }

    @Test
    fun `hentSykefraværprosentAlleNæringer_siste4Kvartaler skal ikke krasje ved manglende data`() {
        val resultat = sykefraværStatistikkNæringRepository.hentForAlleNæringer(
            listOf(ÅrstallOgKvartal(1990, 1))
        )

        resultat shouldBe emptyList()
    }


    @Test
    fun `hentForAlleNæringer skal hente alle næringer for ett kvartal`() {
        arrayOf(ÅrstallOgKvartal(2019, 1), ÅrstallOgKvartal(2019, 2)).forEach {
            sykefraværStatistikkNæringRepository.settInn(
                listOf(
                    SykefraværsstatistikkForNæring(
                        årstall = it.årstall,
                        kvartal = it.kvartal,
                        næring = produksjon.tosifferIdentifikator,
                        antallPersoner = 10,
                        tapteDagsverk = BigDecimal("2.0"),
                        muligeDagsverk = BigDecimal("100.0")
                    ),
                    SykefraværsstatistikkForNæring(
                        årstall = it.årstall,
                        kvartal = it.kvartal,
                        næring = utdanning.tosifferIdentifikator,
                        antallPersoner = 8,
                        tapteDagsverk = BigDecimal("5.0"),
                        muligeDagsverk = BigDecimal("100.0")
                    )
                )
            )
        }


        val resultat = sykefraværStatistikkNæringRepository.hentForAlleNæringer(listOf(ÅrstallOgKvartal(2019, 2)))

        resultat.size shouldBe 2
        resultat[0] shouldBeEqual SykefraværsstatistikkForNæring(
            årstall = 2019,
            kvartal = 2,
            næring = produksjon.tosifferIdentifikator,
            antallPersoner = 10,
            tapteDagsverk = BigDecimal("2.0"),
            muligeDagsverk = BigDecimal("100.0"),
        )
        resultat[1] shouldBeEqual SykefraværsstatistikkForNæring(
            årstall = 2019,
            kvartal = 2,
            næring = utdanning.tosifferIdentifikator,
            antallPersoner = 8,
            tapteDagsverk = BigDecimal("5.0"),
            muligeDagsverk = BigDecimal("100.0"),
        )
    }

    @Test
    fun `hentSykefraværprosentAlleNæringer hentForAlleNæringer skal hente riktig data`() {
        val _2020_1 = ÅrstallOgKvartal(2022, 1)

        sykefraværStatistikkNæringRepository.settInn(
            listOf(
                SykefraværsstatistikkForNæring(
                    årstall = _2020_1.årstall,
                    kvartal = _2020_1.kvartal,
                    næring = "10",
                    antallPersoner = 50,
                    tapteDagsverk = BigDecimal("20000.0"),
                    muligeDagsverk = BigDecimal("1000000.0")
                ),
                SykefraværsstatistikkForNæring(
                    årstall = _2020_1.minusKvartaler(1).årstall,
                    kvartal = _2020_1.minusKvartaler(1).kvartal,
                    næring = "10",
                    antallPersoner = 50,
                    tapteDagsverk = BigDecimal("30000.0"),
                    muligeDagsverk = BigDecimal("1000000.0")
                ),
                SykefraværsstatistikkForNæring(
                    årstall = _2020_1.minusKvartaler(2).årstall,
                    kvartal = _2020_1.minusKvartaler(2).kvartal,
                    næring = "10",
                    antallPersoner = 50,
                    tapteDagsverk = BigDecimal("40000.0"),
                    muligeDagsverk = BigDecimal("1000000.0")
                ),
                SykefraværsstatistikkForNæring(
                    årstall = _2020_1.årstall,
                    kvartal = _2020_1.kvartal,
                    næring = "88",
                    antallPersoner = 50,
                    tapteDagsverk = BigDecimal("25000.0"),
                    muligeDagsverk = BigDecimal("1000000.0")
                ),
            )
        )
        val forventet = listOf(
            SykefraværsstatistikkForNæring(
                årstall = _2020_1.årstall,
                kvartal = _2020_1.kvartal,
                næring = "10",
                antallPersoner = 50,
                tapteDagsverk = BigDecimal("20000.0"),
                muligeDagsverk = BigDecimal("1000000.0")
            ),
            SykefraværsstatistikkForNæring(
                årstall = _2020_1.årstall,
                kvartal = _2020_1.kvartal,
                næring = "88",
                antallPersoner = 50,
                tapteDagsverk = BigDecimal("25000.0"),
                muligeDagsverk = BigDecimal("1000000.0")
            ),
            SykefraværsstatistikkForNæring(
                årstall = _2020_1.minusKvartaler(1).årstall,
                kvartal = _2020_1.minusKvartaler(1).kvartal,
                næring = "10",
                antallPersoner = 50,
                tapteDagsverk = BigDecimal("30000.0"),
                muligeDagsverk = BigDecimal("1000000.0")
            ),
        )
        val resultat: List<SykefraværsstatistikkForNæring> =
            sykefraværStatistikkNæringRepository.hentForAlleNæringer(_2020_1 inkludertTidligere 1)

        resultat.size shouldBe 3
        resultat shouldContainExactlyInAnyOrder forventet
    }

}