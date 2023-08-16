package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ImportEksportJobb
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
import org.jetbrains.exposed.sql.deleteAll
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
open class ImportEksportStatusRepositoryTest {
    @Autowired
    private lateinit var repo: ImportEksportStatusRepository


    private val årstallOgKvartal = ÅrstallOgKvartal(2023, 3)
    private val importEksportStatus = ImportEksportStatusDao(
        årstall = årstallOgKvartal.årstall.toString(),
        kvartal = årstallOgKvartal.kvartal.toString(),
        importertStatistikk = true,
        importertVirksomhetsdata = false,
        eksportertPåKafka = false,
        forberedtNesteEksport = false,
    )

    @AfterEach
    fun afterEach() {
        repo.slettAlt()
    }

    @Test
    fun `sett import eksport status burde sette ny status`() {
        repo.hentImportEksportStatus(årstallOgKvartal) shouldHaveSize 0

        repo.settImportEksportStatus(importEksportStatus)

        repo.hentImportEksportStatus(årstallOgKvartal) shouldContainExactly listOf(importEksportStatus)
    }

    @Test
    fun `sett import eksport status burde oppdatere eksisterende status`() {
        val oppdatertImportEksportStatus = importEksportStatus.copy(importertVirksomhetsdata = true)

        repo.settImportEksportStatus(importEksportStatus)
        repo.settImportEksportStatus(oppdatertImportEksportStatus)

        repo.hentImportEksportStatus(årstallOgKvartal) shouldContainExactly listOf(oppdatertImportEksportStatus)
    }

    @Test
    fun `marker jobb som kjørt burde markere en jobb hvis raden ikke finnes`() {
        repo.markerJobbSomKjørt(årstallOgKvartal, ImportEksportJobb.IMPORTERT_STATISTIKK)

        val resultat = repo.hentImportEksportStatus(årstallOgKvartal).first()

        resultat shouldBeEqual ImportEksportStatusDao(
            årstall = årstallOgKvartal.årstall.toString(),
            kvartal = årstallOgKvartal.kvartal.toString(),
            importertStatistikk = true,
            importertVirksomhetsdata = false,
            forberedtNesteEksport = false,
            eksportertPåKafka = false,
        )
    }

    @Test
    fun `marker jobb som kjørt burde markere en jobb hvis raden finnes`() {
        repo.markerJobbSomKjørt(årstallOgKvartal, ImportEksportJobb.IMPORTERT_STATISTIKK)
        repo.markerJobbSomKjørt(årstallOgKvartal, ImportEksportJobb.IMPORTERT_VIRKSOMHETDATA)

        val resultat = repo.hentImportEksportStatus(årstallOgKvartal).first()

        resultat shouldBeEqual ImportEksportStatusDao(
            årstall = årstallOgKvartal.årstall.toString(),
            kvartal = årstallOgKvartal.kvartal.toString(),
            importertStatistikk = true,
            importertVirksomhetsdata = true,
            forberedtNesteEksport = false,
            eksportertPåKafka = false,
        )
    }

    private fun ImportEksportStatusRepository.slettAlt() {
        transaction(database) {
            deleteAll()
        }
    }
}