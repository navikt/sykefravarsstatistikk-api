package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ImportEksportJobb.IMPORTERT_STATISTIKK
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ImportEksportJobb.IMPORTERT_VIRKSOMHETDATA
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
    private val nyImportEksportStatusDao = ImportEksportStatusDao(
        årstall = årstallOgKvartal.årstall.toString(),
        kvartal = årstallOgKvartal.kvartal.toString(),
        ""
    )

    @AfterEach
    fun afterEach() {
        repo.slettAlt()
    }

    @Test
    fun `sett import eksport status burde sette ny status`() {
        repo.hentFullførteJobber(årstallOgKvartal) shouldHaveSize 0

        repo.leggTilFullførtJobb(IMPORTERT_STATISTIKK, årstallOgKvartal)

        repo.hentFullførteJobber(årstallOgKvartal) shouldContainExactly listOf(
            nyImportEksportStatusDao.copy(
                fullførteJobberKommaseparert = "IMPORTERT_STATISTIKK"
            )
        )
    }

    @Test
    fun `lagre import eksport status burde oppdatere eksisterende status`() {
        val oppdatertImportEksportStatus =
            nyImportEksportStatusDao.copy(fullførteJobberKommaseparert = "IMPORTERT_STATISTIKK,IMPORTERT_VIRKSOMHETDATA")

        repo.lagreImportEksportStatus(oppdatertImportEksportStatus)
        repo.lagreImportEksportStatus(oppdatertImportEksportStatus)

        repo.hentFullførteJobber(årstallOgKvartal) shouldContainExactly listOf(oppdatertImportEksportStatus)
    }

    @Test
    fun `marker jobb som kjørt burde markere en jobb hvis raden ikke finnes`() {
        repo.leggTilFullførtJobb(IMPORTERT_STATISTIKK, årstallOgKvartal)

        val resultat = repo.hentFullførteJobber(årstallOgKvartal).first()

        resultat shouldBeEqual ImportEksportStatusDao(
            årstall = årstallOgKvartal.årstall.toString(),
            kvartal = årstallOgKvartal.kvartal.toString(),
            fullførteJobberKommaseparert = "IMPORTERT_STATISTIKK",
        )
    }

    @Test
    fun `marker jobb som kjørt burde markere en jobb hvis raden finnes`() {
        repo.leggTilFullførtJobb(IMPORTERT_STATISTIKK, årstallOgKvartal)
        repo.leggTilFullførtJobb(IMPORTERT_VIRKSOMHETDATA, årstallOgKvartal)

        val resultat = repo.hentFullførteJobber(årstallOgKvartal).first()

        resultat shouldBeEqual ImportEksportStatusDao(
            årstall = årstallOgKvartal.årstall.toString(),
            kvartal = årstallOgKvartal.kvartal.toString(),
            fullførteJobberKommaseparert = "IMPORTERT_STATISTIKK,IMPORTERT_VIRKSOMHETDATA",
        )
    }

    private fun ImportEksportStatusRepository.slettAlt() {
        transaction(database) {
            deleteAll()
        }
    }
}