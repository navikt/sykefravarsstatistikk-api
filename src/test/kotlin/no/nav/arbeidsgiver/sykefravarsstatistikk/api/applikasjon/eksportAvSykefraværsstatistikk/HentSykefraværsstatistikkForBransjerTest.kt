package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk

import config.AppConfigForJdbcTesterConfig
import ia.felles.definisjoner.bransjer.Bransjer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværStatistikkNæringRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværStatistikkNæringskodeRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.hentSykefraværsstatistikkForBransjer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import testUtils.TestUtils
import java.math.BigDecimal

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class HentSykefraværsstatistikkForBransjerTest {
    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var sykefraværStatistikkNæringRepository: SykefraværStatistikkNæringRepository

    @Autowired
    private lateinit var sykefraværStatistikkNæringskodeRepository: SykefraværStatistikkNæringskodeRepository

    @BeforeEach
    fun beforeEach() {
        TestUtils.slettAllStatistikkFraDatabase(
            jdbcTemplate = jdbcTemplate,
            sykefraværStatistikkNæringRepository = sykefraværStatistikkNæringRepository,
            sykefraværStatistikkNæringskodeRepository = sykefraværStatistikkNæringskodeRepository
        )
    }

    @Test
    fun `hentSykefraværsstatistikkForBransjer skal returnere tom liste når det ikke finnes noen sykefraværsstatistikk`() {
        val result = hentSykefraværsstatistikkForBransjer(
            kvartaler = listOf(ÅrstallOgKvartal(2023, 1)),
            sykefraværsstatistikkNæringRepository = sykefraværStatistikkNæringRepository,
            sykefraværStatistikkNæringskodeRepository = sykefraværStatistikkNæringskodeRepository
        )

        assertThat(result).isEmpty()
    }

    @Test
    fun `henSykefraværsstatistikkForBransjer skal returnere ett kvartal med statistikk`() {
        sykefraværStatistikkNæringRepository.settInn(fireKvartalerAnleggsbransje.map {
            SykefraværsstatistikkForNæring(
                årstall = it.årstall,
                kvartal = it.kvartal,
                næringkode = Næring("42").tosifferIdentifikator,
                antallPersoner = it.antallPersoner,
                tapteDagsverk = it.tapteDagsverk,
                muligeDagsverk = it.muligeDagsverk
            )
        })
        val result = hentSykefraværsstatistikkForBransjer(
            kvartaler = listOf(ÅrstallOgKvartal(2023, 1)),
            sykefraværsstatistikkNæringRepository = sykefraværStatistikkNæringRepository,
            sykefraværStatistikkNæringskodeRepository = sykefraværStatistikkNæringskodeRepository
        )

        assertThat(result).contains(fireKvartalerAnleggsbransje.first())
    }

    @Test
    fun `henSykefraværsstatistikkForBransjer skal for hver bransje summere opp riktig statistikk`() {
        sykefraværStatistikkNæringskodeRepository.settInn(
            listOf("87101", "87102", "86102").map {
                SykefraværsstatistikkForNæringskode(
                    årstall = 2023,
                    kvartal = 1,
                    næringkode5siffer = it,
                    antallPersoner = 1,
                    tapteDagsverk = 1.toBigDecimal(),
                    muligeDagsverk = 1.toBigDecimal(),
                )
            }
        )
        val result = hentSykefraværsstatistikkForBransjer(
            kvartaler = listOf(ÅrstallOgKvartal(2023, 1)),
            sykefraværsstatistikkNæringRepository = sykefraværStatistikkNæringRepository,
            sykefraværStatistikkNæringskodeRepository = sykefraværStatistikkNæringskodeRepository
        )

        assertThat(result).contains(
            SykefraværsstatistikkBransje(
                årstall = 2023,
                kvartal = 1,
                bransje = Bransjer.SYKEHJEM,
                antallPersoner = 2,
                tapteDagsverk = BigDecimal("2.0"),
                muligeDagsverk = BigDecimal("2.0")
            ),
            SykefraværsstatistikkBransje(
                årstall = 2023,
                kvartal = 1,
                bransje = Bransjer.SYKEHUS,
                antallPersoner = 1,
                tapteDagsverk = BigDecimal("1.0"),
                muligeDagsverk = BigDecimal("1.0")
            )
        )
    }
}


val fireKvartalerAnleggsbransje = arrayOf(
    SykefraværsstatistikkBransje(
        2023,
        1,
        Bransjer.ANLEGG,
        1,
        BigDecimal("1.0"),
        BigDecimal("1.0")
    ),
    SykefraværsstatistikkBransje(
        2023,
        2,
        Bransjer.ANLEGG,
        1,
        BigDecimal("1.0"),
        BigDecimal("1.0")
    ),
    SykefraværsstatistikkBransje(
        2023,
        3,
        Bransjer.ANLEGG,
        1,
        BigDecimal("1.0"),
        BigDecimal("1.0")
    ),
    SykefraværsstatistikkBransje(
        2023,
        4,
        Bransjer.ANLEGG,
        1,
        BigDecimal("1.0"),
        BigDecimal("1.0")
    ),
)
