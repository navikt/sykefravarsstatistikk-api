package infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefravarStatistikkVirksomhetGraderingRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.Rectype
import org.jetbrains.exposed.sql.deleteAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.math.BigDecimal

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class SykefravarStatistikkVirksomhetGraderingRepositoryTest {

    @Autowired
    private lateinit var sykefravarStatistikkVirksomhetGraderingRepository: SykefravarStatistikkVirksomhetGraderingRepository

    @BeforeEach
    fun setUpTests() {
        with(sykefravarStatistikkVirksomhetGraderingRepository) { transaction { deleteAll() } }
    }

    @Test
    fun `settInn skal lagre riktige data i tabellen`() {
        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhetMedGradering(
                    årstall = 2020,
                    kvartal = 3,
                    orgnr = "99999999",
                    næring = "10",
                    næringkode = "10100",
                    rectype = Rectype.VIRKSOMHET.kode,
                    tapteDagsverkGradertSykemelding = BigDecimal("3.0"),
                    antallPersoner = 13,
                    tapteDagsverk = BigDecimal("16.0"),
                    muligeDagsverk = BigDecimal("100.0"),
                )
            )
        )

        val resultat = sykefravarStatistikkVirksomhetGraderingRepository.hentForOrgnr(Orgnr("99999999"))

        resultat.size shouldBe 1
        resultat.first() shouldBeEqualToComparingFields UmaskertSykefraværForEttKvartal(
            årstallOgKvartal = ÅrstallOgKvartal(2020, 3),
            dagsverkTeller = BigDecimal("3.0"),
            dagsverkNevner = BigDecimal("16.0"),
            antallPersoner = 13
        )
    }

    @Test
    fun `hentForNæring skal hente ut data på riktig næring`() {
        val data = SykefraværsstatistikkVirksomhetMedGradering(
            årstall = 2020,
            kvartal = 3,
            orgnr = "99999999",
            næring = "10",
            næringkode = "10100",
            rectype = Rectype.VIRKSOMHET.kode,
            tapteDagsverkGradertSykemelding = BigDecimal("3.0"),
            antallPersoner = 13,
            tapteDagsverk = BigDecimal("16.0"),
            muligeDagsverk = BigDecimal("100.0"),
        )

        sykefravarStatistikkVirksomhetGraderingRepository.settInn(
            listOf(data, data.copy(næring = "20", tapteDagsverk = BigDecimal("4.0")))
        )

        val resultat = sykefravarStatistikkVirksomhetGraderingRepository.hentForNæring(Næring("20"))

        resultat.size shouldBe 1
        resultat.first() shouldBeEqualToComparingFields UmaskertSykefraværForEttKvartal(
            årstallOgKvartal = ÅrstallOgKvartal(2020, 3),
            dagsverkTeller = BigDecimal("3.0"),
            dagsverkNevner = BigDecimal("4.0"),
            antallPersoner = 13
        )
    }
}