package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import ia.felles.definisjoner.bransjer.Bransjer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import org.assertj.core.api.Assertions
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
import testUtils.TestUtils.SISTE_PUBLISERTE_KVARTAL
import testUtils.TestUtils.opprettStatistikkForLandExposed
import testUtils.TestUtils.slettAllStatistikkFraDatabase
import java.math.BigDecimal

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class SykefraværRepositoryJdbcTest {
    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var sykefravarStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository

    @Autowired
    private lateinit var sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository

    @Autowired
    private lateinit var sykefraværRepository: SykefraværRepository


    @BeforeEach
    fun setUp() {
        slettAllStatistikkFraDatabase(
            jdbcTemplate = jdbcTemplate,
            sykefravarStatistikkVirksomhetRepository = sykefravarStatistikkVirksomhetRepository,
            sykefraværStatistikkLandRepository = sykefraværStatistikkLandRepository
        )
    }

    @AfterEach
    fun tearDown() {
        slettAllStatistikkFraDatabase(
            jdbcTemplate = jdbcTemplate,
            sykefravarStatistikkVirksomhetRepository = sykefravarStatistikkVirksomhetRepository,
            sykefraværStatistikkLandRepository = sykefraværStatistikkLandRepository
        )
    }

    @Test
    fun hentUmaskertSykefraværForNorge_skal_hente_riktig_data() {

        opprettStatistikkForLandExposed(sykefraværStatistikkLandRepository)
        val kvartaler = ÅrstallOgKvartal.range(SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1), SISTE_PUBLISERTE_KVARTAL)
        val resultat = sykefraværStatistikkLandRepository.hentForKvartaler(kvartaler)

        Assertions.assertThat(resultat.size).isEqualTo(2)
        Assertions.assertThat(resultat)
            .containsExactlyInAnyOrderElementsOf(
                listOf(
                    sykefraværForEtÅrstallOgKvartal(
                        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).årstall,
                        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).kvartal,
                        5
                    ),
                    sykefraværForEtÅrstallOgKvartal(
                        SISTE_PUBLISERTE_KVARTAL.årstall,
                        SISTE_PUBLISERTE_KVARTAL.kvartal,
                        4
                    )
                )
            )
    }

    @Test
    fun hentSykefraværprosentVirksomhet__skal_returnerer_empty_list_dersom_ingen_data_funnet_for_årstall_og_kvartal() {
        persisterDatasetIDb(BARNEHAGE)
        val resultat =
            sykefravarStatistikkVirksomhetRepository.hentUmaskertSykefravær(BARNEHAGE, ÅrstallOgKvartal(2021, 4))
        Assertions.assertThat(resultat.size).isEqualTo(0)
    }

    @Test
    fun hentSykefraværprosentVirksomhet__skal_returnere_riktig_sykefravær() {
        persisterDatasetIDb(BARNEHAGE)
        val resultat =
            sykefravarStatistikkVirksomhetRepository.hentUmaskertSykefravær(BARNEHAGE, ÅrstallOgKvartal(2018, 3))
        Assertions.assertThat(resultat.size).isEqualTo(4)
        Assertions.assertThat(resultat[0]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2018, 3, 6))
        Assertions.assertThat(resultat[3]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 2, 2))
    }

    @Test
    fun hentSykefraværprosentVirksomhet__skal_returnere_riktig_sykefravær_for_ønskede_kvartaler() {
        persisterDatasetIDb(BARNEHAGE)
        val resultat =
            sykefravarStatistikkVirksomhetRepository.hentUmaskertSykefravær(BARNEHAGE, ÅrstallOgKvartal(2019, 1))
        Assertions.assertThat(resultat.size).isEqualTo(2)
        Assertions.assertThat(resultat[0]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 1, 3))
        Assertions.assertThat(resultat[1]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 2, 2))
    }

    @Test
    fun hentUmaskertSykefraværForEttKvartalListe_skal_hente_riktig_data() {
        persisterDatasetIDb(Næring("10"))
        val resultat = sykefraværRepository.hentUmaskertSykefravær(Næring("10"), ÅrstallOgKvartal(2019, 1))
        Assertions.assertThat(resultat.size).isEqualTo(2)
        Assertions.assertThat(resultat[0]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 1, 3))
        Assertions.assertThat(resultat[1]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 2, 2))
    }

    @Test
    fun hentUmaskertSykefravær_skal_hente_riktig_data_for_5sifferBransje() {
        persisterDatasetIDbForBransjeMed5SifferKode(BARNEHAGEBRANSJEN)
        val resultat = sykefraværRepository.hentUmaskertSykefravær(
            BARNEHAGEBRANSJEN, ÅrstallOgKvartal(2019, 1)
        )
        Assertions.assertThat(resultat.size).isEqualTo(2)
        Assertions.assertThat(resultat[0]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 1, 3))
        Assertions.assertThat(resultat[1]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 2, 2))
    }

    private fun persisterDatasetIDb(barnehage: Underenhet.Næringsdrivende) {

        sykefravarStatistikkVirksomhetRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhet(
                    orgnr = barnehage.orgnr.verdi,
                    årstall = 2019,
                    kvartal = 2,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(2),
                    muligeDagsverk = BigDecimal(100),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
                SykefraværsstatistikkVirksomhet(
                    orgnr = "987654321",
                    årstall = 2019,
                    kvartal = 1,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(3),
                    muligeDagsverk = BigDecimal(100),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
                SykefraværsstatistikkVirksomhet(
                    orgnr = barnehage.orgnr.verdi,
                    årstall = 2019,
                    kvartal = 1,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(3),
                    muligeDagsverk = BigDecimal(100),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
                SykefraværsstatistikkVirksomhet(
                    orgnr = barnehage.orgnr.verdi,
                    årstall = 2018,
                    kvartal = 4,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(5),
                    muligeDagsverk = BigDecimal(100),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
                SykefraværsstatistikkVirksomhet(
                    orgnr = barnehage.orgnr.verdi,
                    årstall = 2018,
                    kvartal = 3,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(6),
                    muligeDagsverk = BigDecimal(100),
                    rectype = "2",
                    varighet = Varighetskategori.TOTAL.kode
                ),
            )
        )
    }

    private fun persisterDatasetIDb(næring: Næring) {
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring "
                    + "(arstall, kvartal, naring_kode, antall_personer, tapte_dagsverk, "
                    + "mulige_dagsverk)"
                    + "values "
                    + "(:arstall, :kvartal, :næring, :antall_personer, :tapte_dagsverk, "
                    + ":mulige_dagsverk)",
            parametre(2019, 2, næring, 10, 2, 100)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring "
                    + "(arstall, kvartal, naring_kode, antall_personer, tapte_dagsverk, "
                    + "mulige_dagsverk)"
                    + "values "
                    + "(:arstall, :kvartal, :næring, :antall_personer, :tapte_dagsverk, "
                    + ":mulige_dagsverk)",
            parametre(2019, 1, Næring("94"), 10, 3, 100)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring "
                    + "(arstall, kvartal, naring_kode, antall_personer, tapte_dagsverk, "
                    + "mulige_dagsverk)"
                    + "values "
                    + "(:arstall, :kvartal, :næring, :antall_personer, :tapte_dagsverk, "
                    + ":mulige_dagsverk)",
            parametre(2019, 1, næring, 10, 3, 100)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring "
                    + "(arstall, kvartal, naring_kode, antall_personer, tapte_dagsverk, "
                    + "mulige_dagsverk)"
                    + "values "
                    + "(:arstall, :kvartal, :næring, :antall_personer, :tapte_dagsverk, "
                    + ":mulige_dagsverk)",
            parametre(2018, 4, næring, 10, 5, 100)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring "
                    + "(arstall, kvartal, naring_kode, antall_personer, tapte_dagsverk, "
                    + "mulige_dagsverk)"
                    + "values "
                    + "(:arstall, :kvartal, :næring, :antall_personer, :tapte_dagsverk, "
                    + ":mulige_dagsverk)",
            parametre(2018, 3, næring, 10, 6, 100)
        )
    }

    private fun persisterDatasetIDbForBransjeMed5SifferKode(bransje: Bransje) {
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring5siffer "
                    + "(arstall, kvartal, naring_kode, antall_personer, tapte_dagsverk, "
                    + "mulige_dagsverk)"
                    + "values "
                    + "(:arstall, :kvartal, :næringskode, :antall_personer, :tapte_dagsverk, "
                    + ":mulige_dagsverk)",
            parametre(
                2019,
                2,
                Næringskode(bransje.identifikatorer[0]),
                10,
                2,
                100
            )
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring5siffer "
                    + "(arstall, kvartal, naring_kode, antall_personer, tapte_dagsverk, "
                    + "mulige_dagsverk)"
                    + "values "
                    + "(:arstall, :kvartal, :næringskode, :antall_personer, :tapte_dagsverk, "
                    + ":mulige_dagsverk)",
            parametre(2019, 1, Næringskode("94444"), 10, 3, 100)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring5siffer "
                    + "(arstall, kvartal, naring_kode, antall_personer, tapte_dagsverk, "
                    + "mulige_dagsverk)"
                    + "values "
                    + "(:arstall, :kvartal, :næringskode, :antall_personer, :tapte_dagsverk, "
                    + ":mulige_dagsverk)",
            parametre(
                2019,
                1,
                Næringskode(bransje.identifikatorer[0]),
                10,
                3,
                100
            )
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring5siffer "
                    + "(arstall, kvartal, naring_kode, antall_personer, tapte_dagsverk, "
                    + "mulige_dagsverk)"
                    + "values "
                    + "(:arstall, :kvartal, :næringskode, :antall_personer, :tapte_dagsverk, "
                    + ":mulige_dagsverk)",
            parametre(
                2018,
                4,
                Næringskode(bransje.identifikatorer[0]),
                10,
                5,
                100
            )
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring5siffer "
                    + "(arstall, kvartal, naring_kode, antall_personer, tapte_dagsverk, "
                    + "mulige_dagsverk)"
                    + "values "
                    + "(:arstall, :kvartal, :næringskode, :antall_personer, :tapte_dagsverk, "
                    + ":mulige_dagsverk)",
            parametre(
                2018,
                3,
                Næringskode(bransje.identifikatorer[0]),
                10,
                6,
                100
            )
        )
    }

    private fun parametre(
        årstall: Int, kvartal: Int, antallPersoner: Int, tapteDagsverk: Int, muligeDagsverk: Int
    ): MapSqlParameterSource {
        return MapSqlParameterSource()
            .addValue("arstall", årstall)
            .addValue("kvartal", kvartal)
            .addValue("antall_personer", antallPersoner)
            .addValue("tapte_dagsverk", tapteDagsverk)
            .addValue("mulige_dagsverk", muligeDagsverk)
    }

    private fun parametre(
        årstall: Int,
        kvartal: Int,
        næring: Næring,
        antallPersoner: Int,
        tapteDagsverk: Int,
        muligeDagsverk: Int
    ): MapSqlParameterSource {
        return parametre(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
            .addValue("næring", næring.tosifferIdentifikator)
    }

    private fun parametre(
        årstall: Int,
        kvartal: Int,
        næringskode: Næringskode,
        antallPersoner: Int,
        tapteDagsverk: Int,
        muligeDagsverk: Int
    ): MapSqlParameterSource {
        return parametre(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
            .addValue("næringskode", næringskode.femsifferIdentifikator)
    }

    companion object {
        val BARNEHAGE = Underenhet.Næringsdrivende(
            Orgnr("999999999"),
            Orgnr("1111111111"),
            "test Barnehage",
            Næringskode("88911"),
            10
        )
        val BARNEHAGEBRANSJEN = Bransje(Bransjer.BARNEHAGER)
        private fun sykefraværForEtÅrstallOgKvartal(
            årstall: Int, kvartal: Int, totalTapteDagsverk: Int
        ): UmaskertSykefraværForEttKvartal {
            return sykefraværForEtÅrstallOgKvartal(årstall, kvartal, totalTapteDagsverk, 100, 10)
        }

        private fun sykefraværForEtÅrstallOgKvartal(
            årstall: Int,
            kvartal: Int,
            totalTapteDagsverk: Int,
            totalMuligeDagsverk: Int,
            totalAntallPersoner: Int
        ): UmaskertSykefraværForEttKvartal {
            return UmaskertSykefraværForEttKvartal(
                ÅrstallOgKvartal(årstall, kvartal),
                BigDecimal(totalTapteDagsverk),
                BigDecimal(totalMuligeDagsverk),
                totalAntallPersoner
            )
        }
    }
}
