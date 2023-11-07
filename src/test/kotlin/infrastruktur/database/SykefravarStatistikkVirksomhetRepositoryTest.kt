package infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefravarStatistikkVirksomhetRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
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
open class SykefravarStatistikkVirksomhetRepositoryTest {

    @Autowired
    private lateinit var sykefravarStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository

    @BeforeEach
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
            selectAll().map { SykefraværsstatistikkVirksomhet(
                årstall = it[årstall],
                kvartal = it[kvartal],
                orgnr = it[orgnr],
                varighet = it[varighet].toString(),
                rectype = it[virksomhetstype].toString(),
                antallPersoner = it[antallPersoner],
                tapteDagsverk = it[tapteDagsverk].toBigDecimal(),
                muligeDagsverk = it[muligeDagsverk].toBigDecimal()
            ) }
        }
    }

}