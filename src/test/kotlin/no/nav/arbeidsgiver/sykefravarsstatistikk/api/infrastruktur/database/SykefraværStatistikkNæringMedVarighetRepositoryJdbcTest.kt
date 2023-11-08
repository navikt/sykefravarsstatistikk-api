package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.UmaskertSykefraværForEttKvartalMedVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import testUtils.TestUtils
import java.math.BigDecimal
import ia.felles.definisjoner.bransjer.Bransje as Bransjer

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class SykefraværStatistikkNæringMedVarighetRepositoryJdbcTest {

    @Autowired
    private lateinit var sykefraværStatistikkNæringMedVarighetRepository: SykefraværStatistikkNæringMedVarighetRepository

    @Autowired
    private lateinit var sykefravarStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository

    @BeforeEach
    fun setUp() {
        TestUtils.slettAllStatistikkFraDatabase(
            sykefravarStatistikkVirksomhetRepository = sykefravarStatistikkVirksomhetRepository,
            sykefraværStatistikkNæringMedVarighetRepository = sykefraværStatistikkNæringMedVarighetRepository,
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
        val årstallOgKvartal = ÅrstallOgKvartal(2019, 2)
        sykefraværStatistikkNæringMedVarighetRepository.settInn(
            listOf(
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = årstallOgKvartal.årstall,
                    kvartal = årstallOgKvartal.kvartal,
                    næringkode = barnehager.femsifferIdentifikator,
                    varighet = Varighetskategori.TOTAL.kode,
                    antallPersoner = 1,
                    tapteDagsverk = 0.toBigDecimal(),
                    muligeDagsverk = 10
                        .toBigDecimal()
                )
            )
        )
        sykefraværStatistikkNæringMedVarighetRepository.settInn(
            listOf(
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = årstallOgKvartal.årstall,
                    kvartal = årstallOgKvartal.kvartal,
                    næringkode = barnehager.femsifferIdentifikator,
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                    antallPersoner = 0,
                    tapteDagsverk = 4
                        .toBigDecimal(),
                    muligeDagsverk = 0
                        .toBigDecimal()
                )
            )
        )
        val resultat =
            sykefraværStatistikkNæringMedVarighetRepository.hentSykefraværMedVarighetNæring(barnehager.næring)
        assertThat(resultat.size).isEqualTo(2)
        assertThat(resultat[0])
            .isEqualTo(
                UmaskertSykefraværForEttKvartalMedVarighet(
                    ÅrstallOgKvartal(2019, 2),
                    BigDecimal("4.0"),
                    BigDecimal("0.0"),
                    0,
                    Varighetskategori._1_DAG_TIL_7_DAGER
                )
            )
        assertThat(resultat[1])
            .isEqualTo(
                UmaskertSykefraværForEttKvartalMedVarighet(
                    ÅrstallOgKvartal(2019, 2),
                    BigDecimal("0.0"),
                    BigDecimal("10.0"),
                    1,
                    Varighetskategori.TOTAL
                )
            )
    }

    @Test
    fun hentSykefraværForEttKvartalMedVarighet_for_bransje__skal_returnere_riktig_sykefravær() {
        val sykehus = Næringskode("86101")
        val legetjeneste = Næringskode("86211")
        val årstallOgKvartal = ÅrstallOgKvartal(2019, 2)
        sykefraværStatistikkNæringMedVarighetRepository.settInn(
            listOf(
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = årstallOgKvartal.årstall,
                    kvartal = årstallOgKvartal.kvartal,
                    næringkode = sykehus.femsifferIdentifikator,
                    varighet = Varighetskategori.TOTAL.kode,
                    antallPersoner = 1,
                    tapteDagsverk = 0.toBigDecimal(),
                    muligeDagsverk = 10
                        .toBigDecimal()
                )
            )
        )
        sykefraværStatistikkNæringMedVarighetRepository.settInn(
            listOf(
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = årstallOgKvartal.årstall,
                    kvartal = årstallOgKvartal.kvartal,
                    næringkode = sykehus.femsifferIdentifikator,
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                    antallPersoner = 0,
                    tapteDagsverk = 4
                        .toBigDecimal(),
                    muligeDagsverk = 0
                        .toBigDecimal()
                )
            )
        )
        sykefraværStatistikkNæringMedVarighetRepository.settInn(
            listOf(
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = årstallOgKvartal.årstall,
                    kvartal = årstallOgKvartal.kvartal,
                    næringkode = legetjeneste.femsifferIdentifikator,
                    varighet = Varighetskategori.TOTAL.kode,
                    antallPersoner = 5,
                    tapteDagsverk = 0.toBigDecimal(),
                    muligeDagsverk = 50
                        .toBigDecimal()
                )
            )
        )
        sykefraværStatistikkNæringMedVarighetRepository.settInn(
            listOf(
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = årstallOgKvartal.årstall,
                    kvartal = årstallOgKvartal.kvartal,
                    næringkode = legetjeneste.femsifferIdentifikator,
                    varighet = Varighetskategori._1_DAG_TIL_7_DAGER.kode,
                    antallPersoner = 0,
                    tapteDagsverk = 8
                        .toBigDecimal(),
                    muligeDagsverk = 0
                        .toBigDecimal()
                )
            )
        )
        val resultat =
            sykefraværStatistikkNæringMedVarighetRepository.hentSykefraværMedVarighetBransje(Bransjer.SYKEHUS.bransjeId)
        assertThat(resultat.size).isEqualTo(2)
        assertThat(resultat[0])
            .isEqualTo(
                UmaskertSykefraværForEttKvartalMedVarighet(
                    ÅrstallOgKvartal(2019, 2),
                    BigDecimal("4.0"),
                    BigDecimal("0.0"),
                    0,
                    Varighetskategori._1_DAG_TIL_7_DAGER
                )
            )
        assertThat(resultat[1])
            .isEqualTo(
                UmaskertSykefraværForEttKvartalMedVarighet(
                    ÅrstallOgKvartal(2019, 2),
                    BigDecimal("0.0"),
                    BigDecimal("10.0"),
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
        sykefraværStatistikkNæringMedVarighetRepository.settInn(
            listOf(
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2023,
                    kvartal = 1,
                    næringkode = næringskode1.femsifferIdentifikator,
                    varighet = "E",
                    antallPersoner = 20,
                    tapteDagsverk = 100.toBigDecimal(),
                    muligeDagsverk = 1000.toBigDecimal()
                )
            )
        )
        sykefraværStatistikkNæringMedVarighetRepository.settInn(
            listOf(
                SykefraværsstatistikkNæringMedVarighet(
                    årstall = 2023,
                    kvartal = 1,
                    næringkode = næringskode2.femsifferIdentifikator,
                    varighet = "E",
                    antallPersoner = 20,
                    tapteDagsverk = 400.toBigDecimal(),
                    muligeDagsverk = 1000
                        .toBigDecimal()
                )
            )
        )

        val resultat =
            sykefraværStatistikkNæringMedVarighetRepository.hentSykefraværMedVarighetNæring(næringskode1.næring)

        // Resultatet skal bli statistikk for BEGGE de to næringskodene
        assertThat(resultat.size).isEqualTo(2)
    }
}
