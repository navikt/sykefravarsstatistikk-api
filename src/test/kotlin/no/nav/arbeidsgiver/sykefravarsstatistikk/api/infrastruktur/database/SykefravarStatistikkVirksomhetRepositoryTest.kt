package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import io.kotest.matchers.collections.shouldHaveSize
import config.AppConfigForJdbcTesterConfig
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@ActiveProfiles("db-test")
@ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class SykefravarStatistikkVirksomhetRepositoryTest {
    @Autowired
    private lateinit var repo: SykefravarStatistikkVirksomhetRepository

    val dummyKvartal = ÅrstallOgKvartal(2023, 2)

    @AfterEach
    fun afterEach() {
        repo.slettAlt()
    }

    @Test
    fun `slettDataEldreEnn skal ikke slette data nyere enn gitt årstall og kvartal`() {
        settInnDummydata(dummyKvartal)
        settInnDummydata(dummyKvartal.plussKvartaler(1))
        settInnDummydata(dummyKvartal.plussKvartaler(8))

        repo.slettDataEldreEnn(dummyKvartal)

        repo.hentAlleKvartaler() shouldHaveSize 3
    }

    @Test
    fun `slettDataEldreEnn sletter data eldre enn gitt årstall og kvartal`() {
        settInnDummydata(dummyKvartal.minusKvartaler(1))
        settInnDummydata(dummyKvartal.minusKvartaler(8))

        repo.slettDataEldreEnn(dummyKvartal)

        repo.hentAlleKvartaler() shouldHaveSize 0
    }

    private fun SykefravarStatistikkVirksomhetRepository.slettAlt() {
        transaction {
            deleteAll()
        }
    }

    private fun settInnDummydata(
        årstallOgKvartal: ÅrstallOgKvartal,
        orgnr: String = "999999999",
        antallPersoner: Int = 10,
        tapteDagsverk: Float = 5f,
        muligeDagsverk: Float = 100f,
    ) {
        repo.settInn(
            listOf(
                SykefraværsstatistikkVirksomhet(
                    årstall = årstallOgKvartal.årstall,
                    kvartal = årstallOgKvartal.kvartal,
                    orgnr = orgnr,
                    tapteDagsverk = tapteDagsverk.toBigDecimal(),
                    muligeDagsverk = muligeDagsverk.toBigDecimal(),
                    antallPersoner = antallPersoner,
                    varighet = Varighetskategori.TOTAL.kode,
                    rectype = "2"
                )
            )
        )
    }

    private fun SykefravarStatistikkVirksomhetRepository.hentAlleKvartaler(): List<ÅrstallOgKvartal> {
        return transaction(database) {
            selectAll().map {
                ÅrstallOgKvartal(it[årstall], it[kvartal])
            }
        }
    }
}

