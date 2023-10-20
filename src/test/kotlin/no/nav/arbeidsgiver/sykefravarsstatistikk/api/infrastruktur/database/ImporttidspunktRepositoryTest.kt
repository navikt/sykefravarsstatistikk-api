package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension


@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
internal open class ImporttidspunktRepositoryTest {

    @Autowired
    lateinit var importtidspunktRepository: ImporttidspunktRepository

    @Test
    fun `hentSisteImporttidspunkt skal returnerer nyeste importerte kvartal`() {

        importtidspunktRepository.settInnImporttidspunkt(ÅrstallOgKvartal(2023, 1))
        importtidspunktRepository.settInnImporttidspunkt(ÅrstallOgKvartal(2023, 3))
        importtidspunktRepository.settInnImporttidspunkt(ÅrstallOgKvartal(2022, 4))
        importtidspunktRepository.settInnImporttidspunkt(ÅrstallOgKvartal(2023, 2))

        val resultat = importtidspunktRepository.hentNyesteImporterteKvartal()

        resultat?.gjeldendePeriode shouldBe ÅrstallOgKvartal(2023, 3)
    }
}