package infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import ia.felles.definisjoner.bransjer.Bransjer
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Bransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkForNæringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværStatistikkNæringskodeRepository
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
open class SykefraværStatistikkNæringskodeRepositoryTest {
    @Autowired
    lateinit var sykefraværStatistikkNæringskodeRepository: SykefraværStatistikkNæringskodeRepository

    @Test
    fun `bransje skal regens ut riktig`() {
        sykefraværStatistikkNæringskodeRepository.settInn(
            listOf(
                SykefraværsstatistikkForNæringskode(
                    årstall = 2023,
                    kvartal = 3,
                    næringkode5siffer = "87101",
                    tapteDagsverk = 4.toBigDecimal(),
                    muligeDagsverk = 10.toBigDecimal(),
                    antallPersoner = 10
                ),
                SykefraværsstatistikkForNæringskode(
                    årstall = 2023,
                    kvartal = 3,
                    næringkode5siffer = "87102",
                    tapteDagsverk = 6.toBigDecimal(),
                    muligeDagsverk = 10.toBigDecimal(),
                    antallPersoner = 10,
                ),
                SykefraværsstatistikkForNæringskode(
                    årstall = 2023,
                    kvartal = 3,
                    næringkode5siffer = "88000",
                    tapteDagsverk = 8.toBigDecimal(),
                    muligeDagsverk = 10.toBigDecimal(),
                    antallPersoner = 10,
                )
            )
        )

        val resultat = sykefraværStatistikkNæringskodeRepository.hentForBransje(
            Bransje(Bransjer.SYKEHJEM), listOf(
                ÅrstallOgKvartal(2023, 3)
            )
        )

        resultat shouldBe listOf(
            SykefraværsstatistikkBransje(
                årstall = 2023,
                kvartal = 3,
                bransje = Bransjer.SYKEHJEM,
                tapteDagsverk = BigDecimal("10.0"),
                muligeDagsverk = BigDecimal("20.0"),
                antallPersoner = 20,
            )
        )
    }
}