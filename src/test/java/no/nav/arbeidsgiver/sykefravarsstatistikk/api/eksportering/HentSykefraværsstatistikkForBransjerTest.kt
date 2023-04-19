package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.SykefraværsstatistikkBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.ArbeidsmiljøportalenBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal
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
import java.math.BigDecimal

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class HentSykefraværsstatistikkForBransjerTest {
    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    @BeforeEach
    fun beforeEach() {
        TestUtils.slettAllStatistikkFraDatabase(jdbcTemplate)
    }

    @Test
    fun `hentSykefraværsstatistikkForBransjer skal returnere tom liste når det ikke finnes noen sykefraværsstatistikk`() {
        val result = hentSykefraværsstatistikkForBransjerFraOgMed(ÅrstallOgKvartal(2023, 1), jdbcTemplate)

        assertThat(result).isEmpty()
    }

    @Test
    fun `henSykefraværsstatistikkForBransjer skal returnere fire kvartaler med statistikk`() {
        fireKvartalerAnleggsbransje.forEach {
            TestUtils.opprettStatistikkForNæring(
                jdbcTemplate,
                Næring("42", "Anlegg"),
                it.Årstall,
                it.kvartal,
                it.tapteDagsverk.toInt(),
                it.muligeDagsverk.toInt(),
                it.antallPersoner
            )
        }
        val result = hentSykefraværsstatistikkForBransjerFraOgMed(ÅrstallOgKvartal(2023, 1), jdbcTemplate)

        assertThat(result).contains(*fireKvartalerAnleggsbransje)
    }

    @Test
    fun `henSykefraværsstatistikkForBransjer skal for hver bransje summere opp riktig statistikk`() {
        listOf("87101", "87102", "86102").forEach {
            TestUtils.opprettStatistikkForNæring5Siffer(
                jdbcTemplate,
                Næringskode5Siffer(it, "Noe med sykehjem"),
                2023,
                1,
                1,
                1,
                1
            )
        }
        val result = hentSykefraværsstatistikkForBransjerFraOgMed(ÅrstallOgKvartal(2023, 1), jdbcTemplate)

        assertThat(result).contains(
            SykefraværsstatistikkBransje(
                2023,
                1,
                ArbeidsmiljøportalenBransje.SYKEHJEM,
                2,
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(2)
            ),
            SykefraværsstatistikkBransje(
                2023,
                1,
                ArbeidsmiljøportalenBransje.SYKEHUS,
                1,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(1)
            )
        )
    }
}


val fireKvartalerAnleggsbransje = arrayOf(
    SykefraværsstatistikkBransje(
        2023,
        1,
        ArbeidsmiljøportalenBransje.ANLEGG,
        1,
        BigDecimal.ONE,
        BigDecimal.ONE
    ),
    SykefraværsstatistikkBransje(
        2023,
        2,
        ArbeidsmiljøportalenBransje.ANLEGG,
        1,
        BigDecimal.ONE,
        BigDecimal.ONE
    ),
    SykefraværsstatistikkBransje(
        2023,
        3,
        ArbeidsmiljøportalenBransje.ANLEGG,
        1,
        BigDecimal.ONE,
        BigDecimal.ONE
    ),
    SykefraværsstatistikkBransje(
        2023,
        4,
        ArbeidsmiljøportalenBransje.ANLEGG,
        1,
        BigDecimal.ONE,
        BigDecimal.ONE
    ),
)
