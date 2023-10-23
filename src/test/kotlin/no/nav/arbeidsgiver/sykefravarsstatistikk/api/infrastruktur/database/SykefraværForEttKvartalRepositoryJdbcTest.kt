package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import ia.felles.definisjoner.bransjer.Bransjer
import config.AppConfigForJdbcTesterConfig
import testUtils.TestUtils.SISTE_PUBLISERTE_KVARTAL
import testUtils.TestUtils.opprettStatistikkForLand
import testUtils.TestUtils.slettAllStatistikkFraDatabase
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import org.assertj.core.api.AssertionsForClassTypes
import org.jetbrains.exposed.sql.Database
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
open class SykefraværForEttKvartalRepositoryJdbcTest {
    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate
    private val kvartalsvisSykefraværprosentRepository: KvartalsvisSykefraværRepository by lazy {
        KvartalsvisSykefraværRepository(jdbcTemplate)
    }

    @Autowired
    private lateinit var database: Database
    private val sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository by lazy {
        SykefraværStatistikkLandRepository(database)
    }


    @BeforeEach
    fun setUp() {
        slettAllStatistikkFraDatabase(
            jdbcTemplate,
            sykefraværStatistikkLandRepository = sykefraværStatistikkLandRepository
        )
    }

    @AfterEach
    fun tearDown() {
        slettAllStatistikkFraDatabase(
            jdbcTemplate,
            sykefraværStatistikkLandRepository = sykefraværStatistikkLandRepository
        )
    }

    @Test
    fun hentSykefraværprosentLand__skal_returnere_riktig_sykefravær() {
        opprettStatistikkForLand(sykefraværStatistikkLandRepository)
        val resultat = sykefraværStatistikkLandRepository.hentAlt()
        AssertionsForClassTypes.assertThat(resultat.size).isEqualTo(3)
        AssertionsForClassTypes.assertThat(resultat[0])
            .isEqualTo(
                SykefraværForEttKvartal(
                    SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2),
                    BigDecimal(6),
                    BigDecimal(100),
                    10
                )
            )
    }

    @Test
    fun hentSykefraværprosentSektor__skal_returnere_riktig_sykefravær() {
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_sektor (sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:sektor_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(Sektor.STATLIG, 2019, 2, 10, 2, 100)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_sektor (sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:sektor_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(Sektor.STATLIG, 2019, 1, 10, 3, 100)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_sektor (sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:sektor_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(Sektor.STATLIG, 2018, 4, 10, 4, 100)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_sektor (sektor_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:sektor_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(Sektor.KOMMUNAL, 2018, 4, 10, 5, 100)
        )
        val resultat = kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentSektor(
            Sektor.STATLIG
        )
        AssertionsForClassTypes.assertThat(resultat.size).isEqualTo(3)
        AssertionsForClassTypes.assertThat(resultat[0])
            .isEqualTo(
                SykefraværForEttKvartal(
                    ÅrstallOgKvartal(2018, 4), BigDecimal(4), BigDecimal(100), 10
                )
            )
    }

    @Test
    fun hentSykefraværprosentBedreNæring__skal_returnere_riktig_sykefravær() {
        val produksjonAvKlær = Næring("14")
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring (naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(produksjonAvKlær, 2019, 2, 10, 2, 100)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring (naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(produksjonAvKlær, 2019, 1, 10, 3, 100)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring (naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(Næring("85"), 2018, 4, 10, 5, 100)
        )
        val resultat = kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentNæring(
            produksjonAvKlær
        )
        AssertionsForClassTypes.assertThat(resultat.size).isEqualTo(2)
        AssertionsForClassTypes.assertThat(resultat[0])
            .isEqualTo(
                SykefraværForEttKvartal(
                    ÅrstallOgKvartal(2019, 1), BigDecimal(3), BigDecimal(100), 10
                )
            )
    }

    @Test
    fun hentSykefraværprosentBransje__skal_returnere_riktig_sykefravær() {
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring5siffer "
                    + "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(Næringskode("87101"), 2019, 2, 10, 5, 100)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring5siffer "
                    + "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(Næringskode("87101"), 2019, 1, 10, 1, 100)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring5siffer "
                    + "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(Næringskode("87102"), 2019, 1, 10, 7, 100)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_naring5siffer "
                    + "(naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(Næringskode("87301"), 2018, 4, 10, 6, 100)
        )
        val sykehjem = Bransje(Bransjer.SYKEHJEM)
        val resultat = kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentBransje(sykehjem)
        AssertionsForClassTypes.assertThat(resultat.size).isEqualTo(2)
        AssertionsForClassTypes.assertThat(resultat[0])
            .isEqualTo(
                SykefraværForEttKvartal(
                    ÅrstallOgKvartal(2019, 1), BigDecimal(8), BigDecimal(200), 20
                )
            )
    }

    @Test
    fun hentSykefraværprosentVirksomhet__skal_returnere_riktig_sykefravær() {
        val barnehage = UnderenhetLegacy(
            Orgnr("999999999"),
            Orgnr("1111111111"),
            "test Barnehage",
            Næringskode("88911"),
            10
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(barnehage.orgnr, 2019, 2, 10, 2, 100, Varighetskategori._1_DAG_TIL_7_DAGER)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(Orgnr("987654321"), 2019, 1, 10, 3, 100, Varighetskategori._1_DAG_TIL_7_DAGER)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                    + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
            parametre(barnehage.orgnr, 2018, 4, 10, 5, 100, Varighetskategori._1_DAG_TIL_7_DAGER)
        )
        val resultat = kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentVirksomhet(
            barnehage
        )
        AssertionsForClassTypes.assertThat(resultat.size).isEqualTo(2)
        AssertionsForClassTypes.assertThat(resultat[0])
            .isEqualTo(
                SykefraværForEttKvartal(
                    ÅrstallOgKvartal(2018, 4), BigDecimal(5), BigDecimal(100), 10
                )
            )
    }

    @Test
    fun hentSykefraværprosentVirksomhet__skal_summere_sykefravær_på_varighet() {
        val barnehage = UnderenhetLegacy(
            Orgnr("999999999"),
            Orgnr("1111111111"),
            "test Barnehage",
            Næringskode("88911"),
            10
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk, varighet) "
                    + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk, :varighet)",
            parametre(barnehage.orgnr, 2019, 2, 10, 2, 100, Varighetskategori._1_DAG_TIL_7_DAGER)
        )
        jdbcTemplate.update(
            "insert into sykefravar_statistikk_virksomhet (orgnr, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk, varighet) "
                    + "VALUES (:orgnr, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk, :varighet)",
            parametre(barnehage.orgnr, 2019, 2, 10, 5, 100, Varighetskategori._8_UKER_TIL_20_UKER)
        )
        val resultat = kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentVirksomhet(
            barnehage
        )
        AssertionsForClassTypes.assertThat(resultat)
            .isEqualTo(
                listOf(
                    SykefraværForEttKvartal(
                        ÅrstallOgKvartal(2019, 2), BigDecimal(7), BigDecimal(200), 20
                    )
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
        sektor: Sektor,
        årstall: Int,
        kvartal: Int,
        antallPersoner: Int,
        tapteDagsverk: Int,
        muligeDagsverk: Int
    ): MapSqlParameterSource {
        return parametre(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
            .addValue("sektor_kode", sektor.sektorkode)
    }

    private fun parametre(
        næringskode: Næringskode,
        årstall: Int,
        kvartal: Int,
        antallPersoner: Int,
        tapteDagsverk: Int,
        muligeDagsverk: Int
    ): MapSqlParameterSource {
        return parametre(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
            .addValue("naring_kode", næringskode.femsifferIdentifikator)
    }

    private fun parametre(
        næring: Næring,
        årstall: Int,
        kvartal: Int,
        antallPersoner: Int,
        tapteDagsverk: Int,
        muligeDagsverk: Int
    ): MapSqlParameterSource {
        return parametre(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
            .addValue("naring_kode", næring.tosifferIdentifikator)
    }

    private fun parametre(
        orgnr: Orgnr,
        årstall: Int,
        kvartal: Int,
        antallPersoner: Int,
        tapteDagsverk: Int,
        muligeDagsverk: Int,
        varighet: Varighetskategori
    ): MapSqlParameterSource {
        return parametre(årstall, kvartal, antallPersoner, tapteDagsverk, muligeDagsverk)
            .addValue("orgnr", orgnr.verdi)
            .addValue("varighet", varighet.kode)
    }
}
