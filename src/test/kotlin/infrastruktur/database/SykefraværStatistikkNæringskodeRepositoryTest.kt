package infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import ia.felles.definisjoner.bransjer.Bransje
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Næringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkForNæringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværStatistikkNæringskodeRepository
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
open class SykefraværStatistikkNæringskodeRepositoryTest {
    @Autowired
    lateinit var sykefraværStatistikkNæringskodeRepository: SykefraværStatistikkNæringskodeRepository

    private val produksjonAvKlær = Næringskode("14190")
    private val undervisning = Næringskode("86907")

    @AfterEach
    fun tearDown() {
        with(sykefraværStatistikkNæringskodeRepository) { transaction { deleteAll() } }
    }

    @Test
    fun `hentAltForKvartaler skal returnere riktig data til alle næringer`() {
        listOf(
            ÅrstallOgKvartal(2019, 2), ÅrstallOgKvartal(2019, 1)
        ).forEach { (årstall, kvartal) ->
            sykefraværStatistikkNæringskodeRepository.settInn(
                listOf(
                    SykefraværsstatistikkForNæringskode(
                        årstall = årstall,
                        kvartal = kvartal,
                        næringskode = produksjonAvKlær.femsifferIdentifikator,
                        antallPersoner = 10,
                        tapteDagsverk = BigDecimal(3),
                        muligeDagsverk = BigDecimal(100)
                    )
                )
            )
            sykefraværStatistikkNæringskodeRepository.settInn(
                listOf(
                    SykefraværsstatistikkForNæringskode(
                        årstall = årstall,
                        kvartal = kvartal,
                        næringskode = undervisning.femsifferIdentifikator,
                        antallPersoner = 10,
                        tapteDagsverk = BigDecimal(5),
                        muligeDagsverk = BigDecimal(100)
                    )
                )
            )
        }

        val resultat = sykefraværStatistikkNæringskodeRepository.hentAltForKvartaler(
            listOf(ÅrstallOgKvartal(2019, 2))
        )

        resultat shouldContain SykefraværsstatistikkForNæringskode(
            årstall = 2019,
            kvartal = 2,
            næringskode = produksjonAvKlær.femsifferIdentifikator,
            antallPersoner = 10,
            tapteDagsverk = BigDecimal("3.0"),
            muligeDagsverk = BigDecimal("100.0")
        )

        resultat shouldContain SykefraværsstatistikkForNæringskode(
            årstall = 2019,
            kvartal = 2,
            næringskode = undervisning.femsifferIdentifikator,
            antallPersoner = 10,
            tapteDagsverk = BigDecimal("5.0"),
            muligeDagsverk = BigDecimal("100.0")
        )
    }


    @Test
    fun `bransje skal regens ut riktig`() {
        sykefraværStatistikkNæringskodeRepository.settInn(
            listOf(
                SykefraværsstatistikkForNæringskode(
                    årstall = 2023,
                    kvartal = 3,
                    næringskode = "87101",
                    tapteDagsverk = 4.toBigDecimal(),
                    muligeDagsverk = 10.toBigDecimal(),
                    antallPersoner = 10
                ),
                SykefraværsstatistikkForNæringskode(
                    årstall = 2023,
                    kvartal = 3,
                    næringskode = "87102",
                    tapteDagsverk = 6.toBigDecimal(),
                    muligeDagsverk = 10.toBigDecimal(),
                    antallPersoner = 10,
                ),
                SykefraværsstatistikkForNæringskode(
                    årstall = 2023,
                    kvartal = 3,
                    næringskode = "88000",
                    tapteDagsverk = 8.toBigDecimal(),
                    muligeDagsverk = 10.toBigDecimal(),
                    antallPersoner = 10,
                )
            )
        )

        val resultat = sykefraværStatistikkNæringskodeRepository.hentForBransje(
            Bransje.SYKEHJEM, listOf(
                ÅrstallOgKvartal(2023, 3)
            )
        )

        resultat shouldBe listOf(
            UmaskertSykefraværForEttKvartal(
                årstallOgKvartal = ÅrstallOgKvartal(2023, 3),
                antallPersoner = 20,
                dagsverkTeller = BigDecimal("10.0"),
                dagsverkNevner = BigDecimal("20.0"),
            )
        )
    }
}