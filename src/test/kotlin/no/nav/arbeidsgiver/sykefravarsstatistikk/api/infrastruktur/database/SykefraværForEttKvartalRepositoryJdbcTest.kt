package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import ia.felles.definisjoner.bransjer.Bransjer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import org.assertj.core.api.AssertionsForClassTypes
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
import testUtils.TestUtils.opprettStatistikkForLand
import testUtils.TestUtils.slettAllStatistikkFraDatabase
import java.math.BigDecimal

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class SykefraværForEttKvartalRepositoryJdbcTest {
    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var kvartalsvisSykefraværprosentRepository: KvartalsvisSykefraværRepository

    @Autowired
    private lateinit var sykefraværStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository

    @Autowired
    private lateinit var sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository

    @Autowired
    private lateinit var sykefraværSektorRepository: SykefraværSektorRepository


    @BeforeEach
    fun setUp() {
        slettAllStatistikkFraDatabase(
            jdbcTemplate,
            sykefraværStatistikkLandRepository = sykefraværStatistikkLandRepository,
            sykefravarStatistikkVirksomhetRepository = sykefraværStatistikkVirksomhetRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        slettAllStatistikkFraDatabase(
            jdbcTemplate,
            sykefraværStatistikkLandRepository = sykefraværStatistikkLandRepository,
            sykefravarStatistikkVirksomhetRepository = sykefraværStatistikkVirksomhetRepository,
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

        sykefraværSektorRepository.settInn(
            listOf(
                SykefraværsstatistikkSektor(
                    årstall = 2019,
                    kvartal = 2,
                    sektorkode = Sektor.STATLIG.sektorkode,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(2),
                    muligeDagsverk = BigDecimal(100)
                ),
                SykefraværsstatistikkSektor(
                    årstall = 2019,
                    kvartal = 1,
                    sektorkode = Sektor.STATLIG.sektorkode,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(2),
                    muligeDagsverk = BigDecimal(100)
                ),
                SykefraværsstatistikkSektor(
                    årstall = 2018,
                    kvartal = 4,
                    sektorkode = Sektor.STATLIG.sektorkode,
                    antallPersoner = 10,
                    tapteDagsverk = 4.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),
                SykefraværsstatistikkSektor(
                    årstall = 2018,
                    kvartal = 4,
                    sektorkode = Sektor.KOMMUNAL.sektorkode,
                    antallPersoner = 10,
                    tapteDagsverk = 5.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal()
                ),

                )
        )

        val resultat =
            sykefraværSektorRepository.hentKvartalsvisSykefraværprosent(
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
    fun hentSykefraværprosentNæring__skal_returnere_riktig_sykefravær() {
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
        val barnehage = Underenhet.Næringsdrivende(
            Orgnr("999999999"),
            Orgnr("1111111111"),
            "test Barnehage",
            Næringskode("88911"),
            10
        )

        sykefraværStatistikkVirksomhetRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhet(
                    årstall = 2019,
                    kvartal = 2,
                    orgnr = barnehage.orgnr.verdi,
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                    rectype = "2",
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(2),
                    muligeDagsverk = BigDecimal(100)
                ),
                SykefraværsstatistikkVirksomhet(
                    årstall = 2019,
                    kvartal = 1,
                    orgnr = "987654321",
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                    rectype = "2",
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(3),
                    muligeDagsverk = BigDecimal(100)
                ),
                SykefraværsstatistikkVirksomhet(
                    årstall = 2018,
                    kvartal = 4,
                    antallPersoner = 10,
                    tapteDagsverk = 5.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal(),
                    orgnr = barnehage.orgnr.verdi,
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                    rectype = "2"
                )
            )
        )

        val resultat = sykefraværStatistikkVirksomhetRepository.hentAlt(barnehage.orgnr)

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
        val barnehage = Underenhet.Næringsdrivende(
            Orgnr("999999999"),
            Orgnr("1111111111"),
            "test Barnehage",
            Næringskode("88911"),
            10
        )

        sykefraværStatistikkVirksomhetRepository.settInn(
            listOf(
                SykefraværsstatistikkVirksomhet(
                    årstall = 2019,
                    kvartal = 2,
                    orgnr = barnehage.orgnr.verdi,
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                    rectype = "2",
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal(2),
                    muligeDagsverk = BigDecimal(100)
                ),
                SykefraværsstatistikkVirksomhet(
                    årstall = 2019,
                    kvartal = 2,
                    antallPersoner = 10,
                    tapteDagsverk = 5.toBigDecimal(),
                    muligeDagsverk = 100.toBigDecimal(),
                    orgnr = barnehage.orgnr.verdi,
                    varighet = Varighetskategori._8_UKER_TIL_20_UKER.kode,
                    rectype = "2"
                )
            )
        )

        val resultat = sykefraværStatistikkVirksomhetRepository.hentAlt(barnehage.orgnr)

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
}
