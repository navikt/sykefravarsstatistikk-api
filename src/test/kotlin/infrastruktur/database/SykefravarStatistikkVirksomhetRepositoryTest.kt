package infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhetUtenVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefravarStatistikkVirksomhetRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
import org.jetbrains.exposed.sql.selectAll
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
open class SykefravarStatistikkVirksomhetRepositoryTest {

    @Autowired
    private lateinit var sykefravarStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository

    @AfterEach
    fun setUp() {
        TestUtils.slettAllStatistikkFraDatabase(
            sykefravarStatistikkVirksomhetRepository = sykefravarStatistikkVirksomhetRepository,
        )
    }

    @Test
    fun `settInn skal lagre riktige data i tabellen`() {

        val data = SykefraværsstatistikkVirksomhet(
            årstall = 2019,
            kvartal = 3,
            orgnr = "999999999",
            varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
            rectype = DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            antallPersoner = 1,
            tapteDagsverk = BigDecimal("16.0"),
            muligeDagsverk = BigDecimal("100.0"),
        )

        sykefravarStatistikkVirksomhetRepository.settInn(listOf(data))

        val statistikkIDatabasen = sykefravarStatistikkVirksomhetRepository.hentAlt()

        statistikkIDatabasen.size shouldBe 1
        statistikkIDatabasen.first() shouldBeEqual data

    }

    private fun SykefravarStatistikkVirksomhetRepository.hentAlt(): List<SykefraværsstatistikkVirksomhet> {
        return transaction {
            selectAll().map {
                SykefraværsstatistikkVirksomhet(
                    årstall = it[årstall],
                    kvartal = it[kvartal],
                    orgnr = it[orgnr],
                    varighet = it[varighet],
                    rectype = it[virksomhetstype].toString(),
                    antallPersoner = it[antallPersoner],
                    tapteDagsverk = it[tapteDagsverk].toBigDecimal(),
                    muligeDagsverk = it[muligeDagsverk].toBigDecimal()
                )
            }
        }
    }

    @Test
    fun `hentSykefraværprosentAlleVirksomheter skal hente alle virksomheter for ett eller flere kvartaler`() {
        sykefravarStatistikkVirksomhetRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhet(
                    orgnr = "999999999",
                    årstall = 2018,
                    kvartal = 4,
                    antallPersoner = 7,
                    tapteDagsverk = BigDecimal("12.0"),
                    muligeDagsverk = BigDecimal("100.0"),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
                SykefraværsstatistikkVirksomhet(
                    orgnr = "987654321",
                    årstall = 2019,
                    kvartal = 1,
                    antallPersoner = 40,
                    tapteDagsverk = BigDecimal("20.0"),
                    muligeDagsverk = BigDecimal("115.0"),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
                SykefraværsstatistikkVirksomhet(
                    orgnr = "999999999",
                    årstall = 2019,
                    kvartal = 2,
                    antallPersoner = 4,
                    tapteDagsverk = BigDecimal("9.0"),
                    muligeDagsverk = BigDecimal("100.0"),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
            )
        )

        val resultat = sykefravarStatistikkVirksomhetRepository.hentSykefraværAlleVirksomheter(
            ÅrstallOgKvartal(2019, 2) inkludertTidligere 1
        )

        resultat.size shouldBe 2

        resultat[0] shouldBeEqual SykefraværsstatistikkVirksomhetUtenVarighet(
            årstall = 2019,
            kvartal = 1,
            orgnr = "987654321",
            antallPersoner = 40,
            tapteDagsverk = BigDecimal("20.0"),
            muligeDagsverk = BigDecimal("115.0"),
        )

        resultat[1] shouldBeEqual SykefraværsstatistikkVirksomhetUtenVarighet(
            årstall = 2019,
            kvartal = 2,
            orgnr = "999999999",
            antallPersoner = 4,
            tapteDagsverk = BigDecimal("9.0"),
            muligeDagsverk = BigDecimal("100.0"),
        )
    }

}