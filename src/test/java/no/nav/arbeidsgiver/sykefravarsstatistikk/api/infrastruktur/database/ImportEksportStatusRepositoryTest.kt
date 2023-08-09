package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
    test("init burde inite") {
        val årstallOgKvartal = ÅrstallOgKvartal(2023, 3)
        repo.initStatus(årstallOgKvartal)

        val resultat = repo.hentImportEksportStatus(årstallOgKvartal)

        resultat shouldNotBe null
        resultat shouldContain årstallOgKvartal.årstall.toString()
    }
}) {
    override fun extensions(): List<Extension> = listOf(SpringExtension)
}