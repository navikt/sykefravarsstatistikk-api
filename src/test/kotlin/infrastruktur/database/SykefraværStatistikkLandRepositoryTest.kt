package infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkLand
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværStatistikkLandRepository
import org.junit.jupiter.api.AfterEach
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

open class SykefraværStatistikkLandRepositoryTest {
    @Autowired
    private lateinit var sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository

    @AfterEach
    fun tearDown() {
        TestUtils.slettAllStatistikkFraDatabase(
            sykefraværStatistikkLandRepository = sykefraværStatistikkLandRepository
        )
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

        sykefraværStatistikkLandRepository.hentNyesteKvartal() shouldBe ÅrstallOgKvartal(2019, 2)
    }
}