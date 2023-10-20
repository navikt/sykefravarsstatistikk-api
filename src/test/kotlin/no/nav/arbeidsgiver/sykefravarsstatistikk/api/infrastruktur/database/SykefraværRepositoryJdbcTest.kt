package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import ia.felles.definisjoner.bransjer.Bransjer
import config.AppConfigForJdbcTesterConfig
import testUtils.TestUtils.SISTE_PUBLISERTE_KVARTAL
import testUtils.TestUtils.opprettStatistikkForLand
import testUtils.TestUtils.slettAllStatistikkFraDatabase
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
import java.math.BigDecimal

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class SykefraværRepositoryJdbcTest {
    @Autowired
    private val jdbcTemplate: NamedParameterJdbcTemplate? = null
    private var sykefraværRepository: SykefraværRepository? = null

    @BeforeEach
    fun setUp() {
        sykefraværRepository = SykefraværRepository(jdbcTemplate!!)
        slettAllStatistikkFraDatabase(jdbcTemplate)
    }

    @AfterEach
    fun tearDown() {
        slettAllStatistikkFraDatabase(jdbcTemplate!!)
    }

    @Test
    fun hentUmaskertSykefraværForNorge_skal_hente_riktig_data() {
        opprettStatistikkForLand(jdbcTemplate!!)
        val resultat = sykefraværRepository!!.hentUmaskertSykefraværForNorge(
            SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1)
        )
        Assertions.assertThat(resultat.size).isEqualTo(2)
        Assertions.assertThat<UmaskertSykefraværForEttKvartal>(resultat)
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
        val resultat = sykefraværRepository!!.hentUmaskertSykefravær(BARNEHAGE, ÅrstallOgKvartal(2021, 4))
        Assertions.assertThat(resultat.size).isEqualTo(0)
    }

    @Test
    fun hentSykefraværprosentVirksomhet__skal_returnere_riktig_sykefravær() {
        persisterDatasetIDb(BARNEHAGE)
        val resultat = sykefraværRepository!!.hentUmaskertSykefravær(BARNEHAGE, ÅrstallOgKvartal(2018, 3))
        Assertions.assertThat(resultat.size).isEqualTo(4)
        Assertions.assertThat(resultat[0]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2018, 3, 6))
        Assertions.assertThat(resultat[3]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 2, 2))
    }

    @Test
    fun hentSykefraværprosentVirksomhet__skal_returnere_riktig_sykefravær_for_ønskede_kvartaler() {
        persisterDatasetIDb(BARNEHAGE)
        val resultat = sykefraværRepository!!.hentUmaskertSykefravær(BARNEHAGE, ÅrstallOgKvartal(2019, 1))
        Assertions.assertThat(resultat.size).isEqualTo(2)
        Assertions.assertThat(resultat[0]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 1, 3))
        Assertions.assertThat(resultat[1]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 2, 2))
    }

    @Test
    fun hentUmaskertSykefraværForEttKvartalListe_skal_hente_riktig_data() {
        persisterDatasetIDb(Næring("10"))
        val resultat = sykefraværRepository!!.hentUmaskertSykefravær(Næring("10"), ÅrstallOgKvartal(2019, 1))
        Assertions.assertThat(resultat.size).isEqualTo(2)
        Assertions.assertThat(resultat[0]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 1, 3))
        Assertions.assertThat(resultat[1]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 2, 2))
    }

    @Test
    fun hentUmaskertSykefravær_skal_hente_riktig_data_for_5sifferBransje() {
        persisterDatasetIDbForBransjeMed5SifferKode(BARNEHAGEBRANSJEN)
        val resultat = sykefraværRepository!!.hentUmaskertSykefravær(
            BARNEHAGEBRANSJEN, ÅrstallOgKvartal(2019, 1)
        )
        Assertions.assertThat(resultat.size).isEqualTo(2)
        Assertions.assertThat(resultat[0]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 1, 3))
        Assertions.assertThat(resultat[1]).isEqualTo(sykefraværForEtÅrstallOgKvartal(2019, 2, 2))
    }

    private fun persisterDatasetIDb(barnehage: UnderenhetLegacy) {
        jdbcTemplate!!.update(
            "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, "
                    + "antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk,"
                    + " :mulige_dagsverk)",
            parametre(barnehage.orgnr, 2019, 2, 10, 2, 100)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, "
                    + "antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk,"
                    + " :mulige_dagsverk)",
            parametre(Orgnr("987654321"), 2019, 1, 10, 3, 100)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, "
                    + "antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk,"
                    + " :mulige_dagsverk)",
            parametre(barnehage.orgnr, 2019, 1, 10, 3, 100)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, "
                    + "antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk,"
                    + " :mulige_dagsverk)",
            parametre(barnehage.orgnr, 2018, 4, 10, 5, 100)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, "
                    + "antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk,"
                    + " :mulige_dagsverk)",
            parametre(barnehage.orgnr, 2018, 3, 10, 6, 100)
        )
    }

    private fun persisterDatasetIDb(næring: Næring) {
        jdbcTemplate!!.update(
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
        jdbcTemplate!!.update(
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
        orgnr: Orgnr,
        årstall: Int,
        kvartal: Int,
        antallPersoner: Int,
        tapteDagsverk: Int,
        muligeDagsverk: Int
    ): MapSqlParameterSource {
        return parametre(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
            .addValue("orgnr", orgnr.verdi)
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
        val BARNEHAGE = UnderenhetLegacy(
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
