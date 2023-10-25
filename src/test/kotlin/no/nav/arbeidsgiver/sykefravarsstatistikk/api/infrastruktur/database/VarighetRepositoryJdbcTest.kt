package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import ia.felles.definisjoner.bransjer.Bransjer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.UmaskertSykefraværForEttKvartalMedVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import testUtils.TestUtils
import java.math.BigDecimal

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class VarighetRepositoryJdbcTest {
    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var varighetRepository: VarighetRepository

    @Autowired
    private lateinit var sykefravarStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository

    @BeforeEach
    fun setUp() {
        TestUtils.slettAllStatistikkFraDatabase(
            jdbcTemplate = jdbcTemplate,
            sykefravarStatistikkVirksomhetRepository = sykefravarStatistikkVirksomhetRepository
        )
    }

    @AfterEach
    fun tearDown() {
        TestUtils.slettAllStatistikkFraDatabase(
            jdbcTemplate = jdbcTemplate,
            sykefravarStatistikkVirksomhetRepository = sykefravarStatistikkVirksomhetRepository
        )
    }

    @Test
    fun hentSykefraværForEttKvartalMedVarighet__skal_returnere_riktig_sykefravær() {
        val barnehage = Underenhet.Næringsdrivende(
            Orgnr("999999999"),
            Orgnr("1111111111"),
            "test Barnehage",
            Næringskode("88911"),
            10
        )

        sykefravarStatistikkVirksomhetRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhet(
                    årstall = 2019,
                    kvartal = 2,
                    orgnr = barnehage.orgnr.verdi,
                    tapteDagsverk = BigDecimal(4),
                    muligeDagsverk = BigDecimal(0),
                    antallPersoner = 0,
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                    rectype = "2"
                ),
                SykefraværsstatistikkVirksomhet(
                    årstall = 2019,
                    kvartal = 2,
                    orgnr = barnehage.orgnr.verdi,
                    varighet = Varighetskategori.TOTAL.kode,
                    rectype = "2",
                    antallPersoner = 6,
                    tapteDagsverk = 0.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                )
            )
        )

        val resultat =
            sykefravarStatistikkVirksomhetRepository.hentSykefraværMedVarighet(barnehage.orgnr)
        assertThat(resultat.size).isEqualTo(2)
        assertThat(resultat[0])
            .isEqualTo(
                UmaskertSykefraværForEttKvartalMedVarighet(
                    årstallOgKvartal = ÅrstallOgKvartal(2019, 2),
                    tapteDagsverk = BigDecimal("4.0"),
                    muligeDagsverk = BigDecimal("0.0"),
                    antallPersoner = 0,
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER
                )
            )
        assertThat(resultat[1])
            .isEqualTo(
                UmaskertSykefraværForEttKvartalMedVarighet(
                    ÅrstallOgKvartal(2019, 2),
                    BigDecimal("0.0"),
                    BigDecimal("100.0"),
                    6,
                    Varighetskategori.TOTAL
                )
            )
    }

    @Test
    fun hentSykefraværForEttKvartalMedVarighet_for_næring__skal_returnere_riktig_sykefravær() {
        val barnehager = Næringskode("88911")
        leggTilStatisitkkNæringMedVarighetForTotalVarighetskategori(
            jdbcTemplate, barnehager, ÅrstallOgKvartal(2019, 2), 1, 10
        )
        leggTilStatisitkkNæringMedVarighet(
            jdbcTemplate,
            barnehager,
            ÅrstallOgKvartal(2019, 2),
            Varighetskategori._1_DAG_TIL_7_DAGER,
            4
        )
        val resultat = varighetRepository.hentSykefraværMedVarighet(barnehager.næring)
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
        leggTilStatisitkkNæringMedVarighetForTotalVarighetskategori(
            jdbcTemplate, sykehus, ÅrstallOgKvartal(2019, 2), 1, 10
        )
        leggTilStatisitkkNæringMedVarighet(
            jdbcTemplate,
            sykehus,
            ÅrstallOgKvartal(2019, 2),
            Varighetskategori._1_DAG_TIL_7_DAGER,
            4
        )
        leggTilStatisitkkNæringMedVarighetForTotalVarighetskategori(
            jdbcTemplate, legetjeneste, ÅrstallOgKvartal(2019, 2), 5, 50
        )
        leggTilStatisitkkNæringMedVarighet(
            jdbcTemplate,
            legetjeneste,
            ÅrstallOgKvartal(2019, 2),
            Varighetskategori._1_DAG_TIL_7_DAGER,
            8
        )
        val resultat = varighetRepository.hentSykefraværMedVarighet(Bransje(Bransjer.SYKEHUS))
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

        // Populer databasen med statistikk for to næringskoder, som har felles næring
        leggTilStatisitkkNæringMedVarighet(jdbcTemplate, næringskode1, 2023, 1, "E", 20, 100, 1000)
        leggTilStatisitkkNæringMedVarighet(jdbcTemplate, næringskode2, 2023, 1, "E", 20, 400, 1000)

        // Kjør hentSykefraværMedVarighet() med et Næring-objekt som er opprettet fra en av nærignskodene
        val resultat = varighetRepository.hentSykefraværMedVarighet(næringskode1.næring)

        // Resultatet skal bli statistikk for BEGGE de to næringskodene
        assertThat(resultat.size).isEqualTo(2)
    }
}


fun leggTilStatisitkkNæringMedVarighetForTotalVarighetskategori(
    jdbcTemplate: NamedParameterJdbcTemplate,
    næringskode: Næringskode,
    årstallOgKvartal: ÅrstallOgKvartal,
    antallPersoner: Int,
    muligeDagsverk: Int
) {
    leggTilStatisitkkNæringMedVarighet(
        jdbcTemplate,
        næringskode,
        årstallOgKvartal.årstall,
        årstallOgKvartal.kvartal,
        Varighetskategori.TOTAL.kode,
        antallPersoner,
        0,
        muligeDagsverk
    )
}


fun leggTilStatisitkkNæringMedVarighet(
    jdbcTemplate: NamedParameterJdbcTemplate,
    næringskode: Næringskode,
    årstallOgKvartal: ÅrstallOgKvartal,
    varighetskategori: Varighetskategori,
    tapteDagsverk: Int
) {
    leggTilStatisitkkNæringMedVarighet(
        jdbcTemplate,
        næringskode,
        årstallOgKvartal.årstall,
        årstallOgKvartal.kvartal,
        varighetskategori.kode,
        0,
        tapteDagsverk,
        0
    )
}

fun leggTilStatisitkkNæringMedVarighet(
    jdbcTemplate: NamedParameterJdbcTemplate,
    næringskode: Næringskode,
    årstall: Int,
    kvartal: Int,
    varighet: String?,
    antallPersoner: Int,
    tapteDagsverk: Int,
    muligeDagsverk: Int
) {
    jdbcTemplate.update(
        "insert into sykefravar_statistikk_naring_med_varighet "
                + "(arstall, kvartal, naring_kode, varighet, antall_personer, tapte_dagsverk, "
                + "mulige_dagsverk) "
                + "VALUES ("
                + ":arstall, "
                + ":kvartal, "
                + ":naring_kode, "
                + ":varighet, "
                + ":antall_personer, "
                + ":tapte_dagsverk, "
                + ":mulige_dagsverk)",
        MapSqlParameterSource()
            .addValue("arstall", årstall)
            .addValue("kvartal", kvartal)
            .addValue("naring_kode", næringskode.femsifferIdentifikator)
            .addValue("varighet", varighet)
            .addValue("antall_personer", antallPersoner)
            .addValue("tapte_dagsverk", tapteDagsverk)
            .addValue("mulige_dagsverk", muligeDagsverk)
    )
}

