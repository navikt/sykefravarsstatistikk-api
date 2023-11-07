package infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkSektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværStatistikkSektorRepository
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
import testUtils.TestUtils
import java.math.BigDecimal

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])

open class SykefraværStatistikkSektorRepositoryTest {
    @Autowired
    private lateinit var sykefraværStatistikkSektorRepository: SykefraværStatistikkSektorRepository

    @AfterEach
    fun tearDown() {
        TestUtils.slettAllStatistikkFraDatabase(
            sykefraværStatistikkSektorRepository = sykefraværStatistikkSektorRepository
        )
    }

    @BeforeEach
    fun setUp() {
        TestUtils.slettAllStatistikkFraDatabase(
            sykefraværStatistikkSektorRepository = sykefraværStatistikkSektorRepository
        )
    }

    @Test
    fun `hentForKvartaler skal hente alle sektorer for ett kvartal`() {
        sykefraværStatistikkSektorRepository.settInn(
            listOf(
                SykefraværsstatistikkSektor(
                    sektorkode = Sektor.KOMMUNAL.sektorkode,
                    årstall = 2018,
                    kvartal = 4,
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
        val resultat = sykefraværStatistikkSektorRepository.hentForKvartaler(
            listOf(
                ÅrstallOgKvartal(2019, 1),
                ÅrstallOgKvartal(2019, 2)
            )
        )

        resultat.size shouldBe 3
        resultat[0] shouldBe SykefraværsstatistikkSektor(
            årstall = 2019,
            kvartal = 1,
            sektorkode = Sektor.KOMMUNAL.sektorkode,
            antallPersoner = 40,
            tapteDagsverk = BigDecimal("20.0"),
            muligeDagsverk = BigDecimal("115.0")
        )
        resultat[2] shouldBe SykefraværsstatistikkSektor(
            årstall = 2019,
            kvartal = 2,
            sektorkode = Sektor.PRIVAT.sektorkode,
            antallPersoner = 4,
            tapteDagsverk = BigDecimal("9.0"),
            muligeDagsverk = BigDecimal("100.0")
        )
    }
}