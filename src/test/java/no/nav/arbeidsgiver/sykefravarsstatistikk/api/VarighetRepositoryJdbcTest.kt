package no.nav.arbeidsgiver.sykefravarsstatistikk.api

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.ArbeidsmiljøportalenBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.VarighetRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.math.BigDecimal

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class VarighetRepositoryJdbcTest {
    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    private lateinit var varighetRepository: VarighetRepository

    @BeforeEach
    fun setUp() {
        varighetRepository = VarighetRepository(jdbcTemplate)
        TestUtils.slettAllStatistikkFraDatabase(jdbcTemplate)
    }

    @AfterEach
    fun tearDown() {
        TestUtils.slettAllStatistikkFraDatabase(jdbcTemplate)
    }

    @Test
    fun hentSykefraværForEttKvartalMedVarighet__skal_returnere_riktig_sykefravær() {
        val barnehage = UnderenhetLegacy(
            Orgnr("999999999"),
            Orgnr("1111111111"),
            "test Barnehage",
            Næringskode("88911"),
            10
        )
        VarighetTestUtils.leggTilVirksomhetsstatistikkMedVarighet(
            jdbcTemplate,
            barnehage.orgnr.verdi,
            ÅrstallOgKvartal(2019, 2),
            Varighetskategori._1_DAG_TIL_7_DAGER,
            0,
            4,
            0
        )
        VarighetTestUtils.leggTilVirksomhetsstatistikkMedVarighet(
            jdbcTemplate,
            barnehage.orgnr.verdi,
            ÅrstallOgKvartal(2019, 2),
            Varighetskategori.TOTAL,
            6,
            0,
            100
        )
        val resultat = varighetRepository.hentSykefraværMedVarighet(barnehage)
        assertThat(resultat.size).isEqualTo(2)
        assertThat(resultat[0])
            .isEqualTo(
                UmaskertSykefraværForEttKvartalMedVarighet(
                    ÅrstallOgKvartal(2019, 2),
                    BigDecimal(4),
                    BigDecimal(0),
                    0,
                    Varighetskategori._1_DAG_TIL_7_DAGER
                )
            )
        assertThat(resultat[1])
            .isEqualTo(
                UmaskertSykefraværForEttKvartalMedVarighet(
                    ÅrstallOgKvartal(2019, 2),
                    BigDecimal(0),
                    BigDecimal(100),
                    6,
                    Varighetskategori.TOTAL
                )
            )
    }

    @Test
    fun hentSykefraværForEttKvartalMedVarighet_for_næring__skal_returnere_riktig_sykefravær() {
        val barnehager = Næringskode("88911")
        VarighetTestUtils.leggTilStatisitkkNæringMedVarighetForTotalVarighetskategori(
            jdbcTemplate, barnehager, ÅrstallOgKvartal(2019, 2), 1, 10
        )
        VarighetTestUtils.leggTilStatisitkkNæringMedVarighet(
            jdbcTemplate,
            barnehager,
            ÅrstallOgKvartal(2019, 2),
            Varighetskategori._1_DAG_TIL_7_DAGER,
            4
        )
        val resultat = varighetRepository.hentSykefraværMedVarighet(
            Næring(barnehager.næring.tosifferIdentifikator, "")
        )
        assertThat(resultat.size).isEqualTo(2)
        assertThat(resultat[0])
            .isEqualTo(
                UmaskertSykefraværForEttKvartalMedVarighet(
                    ÅrstallOgKvartal(2019, 2),
                    BigDecimal(4),
                    BigDecimal(0),
                    0,
                    Varighetskategori._1_DAG_TIL_7_DAGER
                )
            )
        assertThat(resultat[1])
            .isEqualTo(
                UmaskertSykefraværForEttKvartalMedVarighet(
                    ÅrstallOgKvartal(2019, 2),
                    BigDecimal(0),
                    BigDecimal(10),
                    1,
                    Varighetskategori.TOTAL
                )
            )
    }

    @Test
    fun hentSykefraværForEttKvartalMedVarighet_for_bransje__skal_returnere_riktig_sykefravær() {
        val sykehus = Næringskode("86101")
        val legetjeneste = Næringskode("86211")
        VarighetTestUtils.leggTilStatisitkkNæringMedVarighetForTotalVarighetskategori(
            jdbcTemplate, sykehus, ÅrstallOgKvartal(2019, 2), 1, 10
        )
        VarighetTestUtils.leggTilStatisitkkNæringMedVarighet(
            jdbcTemplate,
            sykehus,
            ÅrstallOgKvartal(2019, 2),
            Varighetskategori._1_DAG_TIL_7_DAGER,
            4
        )
        VarighetTestUtils.leggTilStatisitkkNæringMedVarighetForTotalVarighetskategori(
            jdbcTemplate, legetjeneste, ÅrstallOgKvartal(2019, 2), 5, 50
        )
        VarighetTestUtils.leggTilStatisitkkNæringMedVarighet(
            jdbcTemplate,
            legetjeneste,
            ÅrstallOgKvartal(2019, 2),
            Varighetskategori._1_DAG_TIL_7_DAGER,
            8
        )
        val resultat = varighetRepository.hentSykefraværMedVarighet(
            Bransje(
                ArbeidsmiljøportalenBransje.SYKEHUS,
                "Sykehus",
                listOf(
                    "86101",
                    "86102",
                    "86104",
                    "86105",
                    "86106",
                    "86107"
                )
            )
        )
        assertThat(resultat.size).isEqualTo(2)
        assertThat(resultat[0])
            .isEqualTo(
                UmaskertSykefraværForEttKvartalMedVarighet(
                    ÅrstallOgKvartal(2019, 2),
                    BigDecimal(4),
                    BigDecimal(0),
                    0,
                    Varighetskategori._1_DAG_TIL_7_DAGER
                )
            )
        assertThat(resultat[1])
            .isEqualTo(
                UmaskertSykefraværForEttKvartalMedVarighet(
                    ÅrstallOgKvartal(2019, 2),
                    BigDecimal(0),
                    BigDecimal(10),
                    1,
                    Varighetskategori.TOTAL
                )
            )
    }

    @Test
    fun `hent sykefravær med varighet for næring burde returnere sykefraværsstatistikk for alle inkluderte næringskoder`() {
        val næringskode1 = Næringskode("84300")
        val næringskode2 = Næringskode("84999")
        val næring1 = Næring(næringskode1.næring.tosifferIdentifikator, "Næring 1")

        // Populer databasen med statistikk for to næringskoder, som har felles næring
        VarighetTestUtils.leggTilStatisitkkNæringMedVarighet(jdbcTemplate, næringskode1, 2023, 1, "E", 20, 100, 1000)
        VarighetTestUtils.leggTilStatisitkkNæringMedVarighet(jdbcTemplate, næringskode2, 2023, 1, "E", 20, 400, 1000)

        // Kjør hentSykefraværMedVarighet() med et Næring-objekt som er opprettet fra en av nærignskodene
        val resultat = varighetRepository.hentSykefraværMedVarighet(næring1)

        // Resultatet skal bli statistikk for BEGGE de to næringskodene
        assertThat(resultat.size).isEqualTo(2)
    }
}
