package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration

@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@ActiveProfiles("db-test")
open class ImportEksportStatusRepositoryTest(repo: ImportEksportStatusRepository) : FunSpec({
    val årstallOgKvartal = ÅrstallOgKvartal(2023, 3)
    val importEksportStatus = ImportEksportStatusDao(
        årstall = årstallOgKvartal.årstall.toString(),
        kvartal = årstallOgKvartal.kvartal.toString(),
        importertStatistikk = true,
        importertVirksomhetsdata = false,
        eksportertPåKafka = false,
        forberedtNesteEksport = false,
    )

    test("sett import eksport status burde sette ny status") {
        repo.hentImportEksportStatus(årstallOgKvartal) shouldHaveSize 0

        repo.settImportEksportStatus(importEksportStatus)

        repo.hentImportEksportStatus(årstallOgKvartal) shouldContainExactly listOf(importEksportStatus)
    }

    test("sett import eksport status burde oppdatere eksisterende status") {
        val oppdatertImportEksportStatus = importEksportStatus.copy(importertVirksomhetsdata = true)

        repo.settImportEksportStatus(importEksportStatus)
        repo.settImportEksportStatus(oppdatertImportEksportStatus)

        repo.hentImportEksportStatus(årstallOgKvartal) shouldContainExactly listOf(oppdatertImportEksportStatus)
    }
}) {
    override fun extensions(): List<Extension> = listOf(SpringExtension)
}