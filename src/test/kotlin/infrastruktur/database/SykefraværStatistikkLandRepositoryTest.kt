package infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkLand
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværStatistikkLandRepository
import org.jetbrains.exposed.sql.deleteAll
import org.junit.jupiter.api.AfterEach
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
open class SykefraværStatistikkLandRepositoryTest {
    @Autowired
    private lateinit var sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository

    @AfterEach
    fun tearDown() {
        with(sykefraværStatistikkLandRepository) { transaction { deleteAll() } }
    }

    @Test
    fun `hentNyesteKvartal skal returnere siste kvartal`() {
        sykefraværStatistikkLandRepository.settInn(
            listOf(
                SykefraværsstatistikkLand(
                    årstall = 2019,
                    kvartal = 1,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(4),
                    muligeDagsverk = BigDecimal(100),
                ),
                SykefraværsstatistikkLand(
                    årstall = 2018,
                    kvartal = 4,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(5),
                    muligeDagsverk = BigDecimal(100)
                )
            )
        )

        sykefraværStatistikkLandRepository.hentNyesteKvartal() shouldBe ÅrstallOgKvartal(2019, 1)
    }

    @Test
    fun `hentSykefraværstatistikkLand returnerer tom liste dersom ingen statistikk er funnet for kvartal`() {
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

        sykefraværStatistikkLandRepository.hentSykefraværstatistikkLand(
            listOf(ÅrstallOgKvartal(2019, 4))
        ) shouldBe emptyList()
    }

    @Test
    fun `hentSykefraværprosentLand skal hente sykefravær land for ett kvartal`() {
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

        sykefraværStatistikkLandRepository.hentSykefraværstatistikkLand(
            listOf(ÅrstallOgKvartal(2019, 4))
        ) shouldBe emptyList()

        val resultat =
            sykefraværStatistikkLandRepository.hentSykefraværstatistikkLand(
                listOf(
                    ÅrstallOgKvartal(2019, 2),
                    ÅrstallOgKvartal(2019, 1)
                )
            )

        resultat[0] shouldBe SykefraværsstatistikkLand(
            årstall = 2019,
            kvartal = 1,
            antallPersoner = 2750000,
            tapteDagsverk = BigDecimal("350000.0"),
            muligeDagsverk = BigDecimal("71000000.0")
        )

        resultat[1] shouldBe SykefraværsstatistikkLand(
            årstall = 2019,
            kvartal = 2,
            antallPersoner = 2500000,
            tapteDagsverk = BigDecimal("256800.0"),
            muligeDagsverk = BigDecimal("60000000.0")
        )
    }
}