package infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.VirksomhetMetadata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.VirksomhetMetadataRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.Rectype
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
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
open class VirksomhetMetadataRepositoryJdbcTest {

    @Autowired
    private lateinit var repository: VirksomhetMetadataRepository

    private val ORGNR_VIRKSOMHET_1 = "987654321"
    private val ORGNR_VIRKSOMHET_2 = "999999999"
    private val ORGNR_VIRKSOMHET_3 = "999999777"
    private val NÆRINGSKODE_5SIFFER = "10062"
    private val NÆRINGSKODE_2SIFFER = "10"

    @BeforeEach
    fun setUp() {
        repository.slettVirksomhetMetadata()
    }

    @Test
    fun slettVirksomhetMetadata__sletter_VirksomhetMetaData() {
        val (årstall, kvartal) = ÅrstallOgKvartal(2020, 3)
        repository.opprettVirksomhetMetadata(
            listOf(
                VirksomhetMetadata(
                    Orgnr(ORGNR_VIRKSOMHET_1),
                    "Virksomhet 1",
                    "2",
                    Sektor.PRIVAT,
                    "71",
                    "71000",
                    ÅrstallOgKvartal(årstall, kvartal)
                ),
                VirksomhetMetadata(
                    Orgnr(ORGNR_VIRKSOMHET_2),
                    "Virksomhet 2",
                    "2",
                    Sektor.PRIVAT,
                    "10",
                    "10000",
                    ÅrstallOgKvartal(årstall, kvartal)
                ),
                VirksomhetMetadata(
                    Orgnr(ORGNR_VIRKSOMHET_3),
                    "Virksomhet 3",
                    "2",
                    Sektor.PRIVAT,
                    "10",
                    "10000",
                    ÅrstallOgKvartal(årstall, kvartal)
                )
            )
        )
        val antallSlettet = repository.slettVirksomhetMetadata()
        Assertions.assertThat(antallSlettet).isEqualTo(3)
    }

    @Test
    fun opprettVirksomhetMetadata__oppretter_riktig_metadata() {
        val virksomhetMetadataVirksomhet1 = VirksomhetMetadata(
            Orgnr(ORGNR_VIRKSOMHET_1),
            "Virksomhet 1",
            Rectype.VIRKSOMHET.kode,
            Sektor.PRIVAT,
            NÆRINGSKODE_2SIFFER,
            NÆRINGSKODE_5SIFFER,
            ÅrstallOgKvartal(2020, 3)
        )
        val virksomhetMetadataVirksomhet2 = VirksomhetMetadata(
            Orgnr(ORGNR_VIRKSOMHET_2),
            "Virksomhet 2",
            Rectype.VIRKSOMHET.kode,
            Sektor.PRIVAT,
            NÆRINGSKODE_2SIFFER,
            NÆRINGSKODE_5SIFFER,
            ÅrstallOgKvartal(2020, 3)
        )
        repository.opprettVirksomhetMetadata(
            listOf(virksomhetMetadataVirksomhet1, virksomhetMetadataVirksomhet2)
        )
        val results = repository.hentVirksomhetMetadata(ÅrstallOgKvartal(2020, 3))
        Assertions.assertThat(results[0]).isEqualTo(virksomhetMetadataVirksomhet1)
        Assertions.assertThat(results[1]).isEqualTo(virksomhetMetadataVirksomhet2)
    }

}
