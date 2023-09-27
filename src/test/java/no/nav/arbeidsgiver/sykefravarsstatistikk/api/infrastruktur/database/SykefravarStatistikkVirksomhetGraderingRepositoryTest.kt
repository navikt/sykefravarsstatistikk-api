package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import io.kotest.matchers.collections.shouldHaveSize
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næringskode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
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
open class SykefravarStatistikkVirksomhetGraderingRepositoryTest {
    @Autowired
    private lateinit var repo: SykefravarStatistikkVirksomhetGraderingRepository

    val dummyKvartal = ÅrstallOgKvartal(2023, 2)

    @AfterEach
    fun afterEach() {
        repo.slettAlt()
    }

    @Test
    fun `slettDataEldreEnn skal ikke slette data nyere enn gitt årstall og kvartal`() {
        repo.settInn(dummyKvartal)
        repo.settInn(dummyKvartal.plussKvartaler(1))
        repo.settInn(dummyKvartal.plussKvartaler(8))

        repo.slettDataEldreEnn(dummyKvartal)

        repo.hentAlt() shouldHaveSize 3
    }

    @Test
    fun `slettDataEldreEnn sletter data eldre enn gitt årstall og kvartal`() {
        repo.settInn(dummyKvartal.minusKvartaler(1))
        repo.settInn(dummyKvartal.minusKvartaler(8))

        repo.slettDataEldreEnn(dummyKvartal)

        repo.hentAlt() shouldHaveSize 0
    }

    private fun SykefravarStatistikkVirksomhetGraderingRepository.slettAlt() {
        transaction {
            deleteAll()
        }
    }

    private fun SykefravarStatistikkVirksomhetGraderingRepository.settInn(
        årstallOgKvartal: ÅrstallOgKvartal,
        orgnr: String = "999999999",
        næringskode: Næringskode = Næringskode("99999"),
        antallGraderteSykemeldinger: Int = 10,
        tapteDagsverkGradertSykemelding: Float = 5f,
        antallSykemeldinger: Int = 10,
        antallPersoner: Int = 10,
        tapteDagsverk: Float = 5f,
        muligeDagsverk: Float = 100f,
    ) {
        transaction {
            insert {
                it[this.orgnr] = orgnr
                it[this.næring] = næringskode.næring.tosifferIdentifikator
                it[this.næringskode] = næringskode.femsifferIdentifikator
                it[this.årstall] = årstallOgKvartal.årstall
                it[this.kvartal] = årstallOgKvartal.kvartal
                it[this.antallGraderteSykemeldinger] = antallGraderteSykemeldinger
                it[this.tapteDagsverkGradertSykemelding] = tapteDagsverkGradertSykemelding
                it[this.antallSykemeldinger] = antallSykemeldinger
                it[this.antallPersoner] = antallPersoner
                it[this.tapteDagsverk] = tapteDagsverk
                it[this.muligeDagsverk] = muligeDagsverk
            }
        }
    }

    private fun SykefravarStatistikkVirksomhetGraderingRepository.hentAlt(): List<ÅrstallOgKvartal> {
        return org.jetbrains.exposed.sql.transactions.transaction(database) {
            selectAll().map {
                ÅrstallOgKvartal(it[årstall], it[kvartal])
            }
        }
    }
}