package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron.ImportEksportJobb.EKSPORTERT_METADATA_VIRKSOMHET
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron.ImportEksportJobb.EKSPORTERT_PER_STATISTIKKATEGORI
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron.ImportEksportJobb.IMPORTERT_STATISTIKK
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron.ImportEksportJobb.IMPORTERT_VIRKSOMHETDATA
import org.jetbrains.exposed.sql.deleteAll
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
open class ImportEksportStatusRepositoryTest {
    @Autowired
    private lateinit var repo: ImportEksportStatusRepository

    private val årstallOgKvartal = ÅrstallOgKvartal(2023, 3)

    @AfterEach
    fun afterEach() {
        repo.slettAlt()
    }

    @Test
    fun `returnerer en tom liste dersom ÅrstallOgKvartal ikke er i tabellen`() {
        lagreAlleJobber(ÅrstallOgKvartal(2023, 1))
        lagreAlleJobber(ÅrstallOgKvartal(2023, 2))
        lagreAlleJobber(ÅrstallOgKvartal(2023, 3))
        lagreAlleJobber(ÅrstallOgKvartal(2023, 4))

        val resultat = repo.hentFullførteJobber(ÅrstallOgKvartal(2024, 1))

        resultat shouldBe emptyList()
    }

    fun lagreAlleJobber(kvartal: ÅrstallOgKvartal) {
        listOf(
            IMPORTERT_STATISTIKK, IMPORTERT_VIRKSOMHETDATA, EKSPORTERT_METADATA_VIRKSOMHET,
            EKSPORTERT_PER_STATISTIKKATEGORI
        ).forEach {
            repo.leggTilFullførtJobb(it, kvartal)
        }
    }

    @Test
    fun `marker jobb som kjørt burde markere en jobb hvis raden ikke finnes`() {
        repo.leggTilFullførtJobb(IMPORTERT_STATISTIKK, årstallOgKvartal)

        val resultat = repo.hentFullførteJobber(årstallOgKvartal)

        resultat shouldContainExactly listOf(IMPORTERT_STATISTIKK)
    }

    @Test
    fun `marker jobb som kjørt burde markere en jobb hvis raden finnes`() {
        repo.leggTilFullførtJobb(IMPORTERT_STATISTIKK, årstallOgKvartal)
        repo.leggTilFullførtJobb(IMPORTERT_VIRKSOMHETDATA, årstallOgKvartal)

        val resultat = repo.hentFullførteJobber(årstallOgKvartal)

        resultat shouldContainExactly listOf(IMPORTERT_STATISTIKK, IMPORTERT_VIRKSOMHETDATA)
    }

    private fun ImportEksportStatusRepository.slettAlt() {
        transaction {
            deleteAll()
        }
    }
}