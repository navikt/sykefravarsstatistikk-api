package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk

import arrow.core.right
import ia.felles.definisjoner.bransjer.Bransje
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.UmaskertSykefraværForEttKvartalMedVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.tilgangsstyring.TilgangskontrollService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.EnhetsregisteretClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testUtils.TestUtils
import java.math.BigDecimal
import java.time.LocalDate

internal class AggregertStatistikkServiceTest {

    private val mockImporttidspunktRepository = mockk<ImporttidspunktRepository>()

    private val mockTilgangskontrollService = mockk<TilgangskontrollService>()

    private val mockEnhetsregisteretClient = mockk<EnhetsregisteretClient>()

    private val mockSykefraværStatistikkNæringMedVarighetRepository = mockk<SykefraværStatistikkNæringMedVarighetRepository>()
    private val mockSykefravarStatistikkVirksomhetGraderingRepository =
        mockk<SykefravarStatistikkVirksomhetGraderingRepository>()
    val mockSykefravarStatistikkVirksomhetRepository = mockk<SykefravarStatistikkVirksomhetRepository>()
    val mockSykefraværStatistikkLandRepository = mockk<SykefraværStatistikkLandRepository>()
    val mockSykefraværStatistikkNæringRepository = mockk<SykefraværStatistikkNæringRepository>()
    val mockSykefraværStatistikkNæringskodeRepository = mockk<SykefraværStatistikkNæringskodeRepository>()

    private val serviceUnderTest: AggregertStatistikkService = AggregertStatistikkService(
        mockSykefraværStatistikkNæringMedVarighetRepository,
        mockTilgangskontrollService,
        mockEnhetsregisteretClient,
        mockImporttidspunktRepository,
        mockSykefravarStatistikkVirksomhetGraderingRepository,
        mockSykefravarStatistikkVirksomhetRepository,
        mockSykefraværStatistikkLandRepository,
        mockSykefraværStatistikkNæringRepository,
        mockSykefraværStatistikkNæringskodeRepository,
    )

    @BeforeEach
    fun setUp() {
        every { mockImporttidspunktRepository.hentNyesteImporterteKvartal() } returns Importtidspunkt(
            LocalDate.of(
                2022,
                2,
                2
            ), TestUtils.SISTE_PUBLISERTE_KVARTAL
        )

        every { mockSykefravarStatistikkVirksomhetRepository.hentUmaskertSykefravær(any(), any()) } returns listOf()
        every { mockSykefravarStatistikkVirksomhetRepository.hentSykefraværMedVarighet(any()) } returns listOf()
        every { mockSykefraværStatistikkNæringRepository.hentForKvartaler(any(), any()) } returns listOf()
        every { mockSykefraværStatistikkNæringskodeRepository.hentForBransje(any(), any()) } returns listOf()
        every { mockSykefraværStatistikkLandRepository.hentForKvartaler(any()) } returns listOf()

        every { mockSykefravarStatistikkVirksomhetGraderingRepository.hentForOrgnr(any()) } returns listOf()
        every { mockSykefravarStatistikkVirksomhetGraderingRepository.hentForBransje(any()) } returns listOf()
        every { mockSykefravarStatistikkVirksomhetGraderingRepository.hentForNæring(any()) } returns listOf()

        every { mockSykefraværStatistikkNæringMedVarighetRepository.hentSykefraværMedVarighetNæring(any()) } returns listOf()
        every { mockSykefraværStatistikkNæringMedVarighetRepository.hentSykefraværMedVarighetBransje(any()) } returns listOf()
    }

    @Test
    fun hentAggregertHistorikk_kræsjerIkkeVedManglendeData() {

        every { mockTilgangskontrollService.brukerRepresentererVirksomheten(any()) } returns true
        every { mockEnhetsregisteretClient.hentUnderenhet(any()) } returns enBarnehage.right()
        every { mockTilgangskontrollService.brukerHarIaRettigheterIVirksomheten(any()) } returns true

        // Helt tomt resultat skal ikke kræsje
        serviceUnderTest.hentAggregertStatistikk(etOrgnr).getOrNull()!! shouldBeEqual AggregertStatistikkJson()
    }

    @Test
    fun hentAggregertHistorikk_henterAltSykefraværDersomBrukerHarIaRettigheter() {
        mockAvhengigheterForBarnehageMedIaRettigheter()

        every {
            mockSykefravarStatistikkVirksomhetRepository.hentUmaskertSykefravær(any(), any())
        } returns genererTestSykefravær(1)

        every {
            mockSykefraværStatistikkLandRepository.hentForKvartaler(any())
        } returns genererTestSykefravær(10)

        every {
            mockSykefraværStatistikkNæringskodeRepository.hentForBransje(any(), any())
        } returns genererTestSykefravær(30).map {
            SykefraværsstatistikkBransje(
                årstall = it.årstallOgKvartal.årstall,
                kvartal = it.årstallOgKvartal.kvartal,
                bransje = Bransje.BARNEHAGER,
                antallPersoner = it.antallPersoner,
                tapteDagsverk = it.dagsverkTeller,
                muligeDagsverk = it.dagsverkNevner
            )
        }

        every {
            mockSykefravarStatistikkVirksomhetGraderingRepository.hentForBransje(any())
        } returns genererTestSykefravær(70)

        every {
            mockSykefravarStatistikkVirksomhetGraderingRepository.hentForOrgnr(any())
        } returns genererTestSykefravær(50)

        val forventedeProsenttyper =
            listOf(Statistikkategori.VIRKSOMHET, Statistikkategori.BRANSJE, Statistikkategori.LAND)
        val forventedeGraderingstyper = listOf(Statistikkategori.VIRKSOMHET, Statistikkategori.BRANSJE)
        val forventedeTrendtyper = listOf(Statistikkategori.BRANSJE)

        val resultat = serviceUnderTest.hentAggregertStatistikk(etOrgnr).getOrNull()!!

        val prosentstatistikk = resultat.prosentSiste4KvartalerTotalt.map { it.statistikkategori }.toList()
        val gradertProsentstatistikk = resultat.prosentSiste4KvartalerGradert.map { it.statistikkategori }.toList()
        val trendstatistikk = resultat.trendTotalt.map { it.statistikkategori }.toList()

        prosentstatistikk shouldBeEqual forventedeProsenttyper
        gradertProsentstatistikk shouldBeEqual forventedeGraderingstyper
        trendstatistikk shouldBeEqual forventedeTrendtyper
    }

    @Test
    fun hentAggregertStatistikk_regnerUtLangtidsfravær_dersomAntallPersonerErOverEllerLikFem() {
        mockAvhengigheterForBarnehageMedIaRettigheter()

        every {
            mockSykefravarStatistikkVirksomhetRepository.hentUmaskertSykefravær(any(), any())
        } returns genererTestSykefravær(1)

        every { mockSykefraværStatistikkNæringMedVarighetRepository.hentSykefraværMedVarighetNæring(any()) }

        every {
            mockSykefravarStatistikkVirksomhetRepository.hentSykefraværMedVarighet(any())
        } returns
                listOf(
                    UmaskertSykefraværForEttKvartalMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL,
                        BigDecimal(10),
                        BigDecimal(0),
                        0,
                        Varighetskategori._20_UKER_TIL_39_UKER
                    ), UmaskertSykefraværForEttKvartalMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL,
                        BigDecimal(40),
                        BigDecimal(0),
                        0,
                        Varighetskategori._17_DAGER_TIL_8_UKER
                    ), UmaskertSykefraværForEttKvartalMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL,
                        BigDecimal(20),
                        BigDecimal(0),
                        0,
                        Varighetskategori._8_UKER_TIL_20_UKER
                    ), UmaskertSykefraværForEttKvartalMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL,
                        BigDecimal(5),
                        BigDecimal(0),
                        0,
                        Varighetskategori._8_DAGER_TIL_16_DAGER
                    ), UmaskertSykefraværForEttKvartalMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL,
                        BigDecimal(0),
                        BigDecimal(100),
                        200,
                        Varighetskategori.TOTAL
                    ), UmaskertSykefraværForEttKvartalMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1),
                        BigDecimal(10),
                        BigDecimal(0),
                        0,
                        Varighetskategori._20_UKER_TIL_39_UKER
                    ), UmaskertSykefraværForEttKvartalMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1),
                        BigDecimal(0),
                        BigDecimal(100),
                        5,
                        Varighetskategori.TOTAL
                    )
                )


        val forventet = StatistikkJson(
            Statistikkategori.VIRKSOMHET,
            "En Barnehage",
            "40.0",
            200,
            listOf(TestUtils.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1), TestUtils.SISTE_PUBLISERTE_KVARTAL)
        )

        serviceUnderTest.hentAggregertStatistikk(etOrgnr)
            .getOrNull()?.prosentSiste4KvartalerLangtid!![0] shouldBeEqual forventet
    }

    @Test
    fun hentAggregertStatistikk_returnererKorttidsfravær_dersomAntallPersonerErOverEllerLikFem() {
        mockAvhengigheterForBarnehageMedIaRettigheter()
        every {
            mockSykefravarStatistikkVirksomhetRepository.hentUmaskertSykefravær(any(), any())
        } returns genererTestSykefravær(1)

        every {
            mockSykefravarStatistikkVirksomhetRepository.hentSykefraværMedVarighet(any())
        } returns
                listOf(
                    UmaskertSykefraværForEttKvartalMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL,
                        BigDecimal(40),
                        BigDecimal(0),
                        0,
                        Varighetskategori._1_DAG_TIL_7_DAGER
                    ), UmaskertSykefraværForEttKvartalMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL,
                        BigDecimal(5),
                        BigDecimal(0),
                        0,
                        Varighetskategori._8_DAGER_TIL_16_DAGER
                    ), UmaskertSykefraværForEttKvartalMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL,
                        BigDecimal(0),
                        BigDecimal(100),
                        100,
                        Varighetskategori.TOTAL
                    ), UmaskertSykefraværForEttKvartalMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1),
                        BigDecimal(10),
                        BigDecimal(0),
                        0,
                        Varighetskategori._8_DAGER_TIL_16_DAGER
                    ), UmaskertSykefraværForEttKvartalMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1),
                        BigDecimal(0),
                        BigDecimal(100),
                        0,
                        Varighetskategori.TOTAL
                    )
                )

        val resultat = serviceUnderTest.hentAggregertStatistikk(etOrgnr)
            .getOrNull()?.prosentSiste4KvartalerKorttid!![0]

        val forventet = StatistikkJson(
            Statistikkategori.VIRKSOMHET,
            "En Barnehage",
            "27.5",
            100,
            listOf(TestUtils.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1), TestUtils.SISTE_PUBLISERTE_KVARTAL)
        )

        resultat shouldBeEqual forventet
    }

    @Test
    fun hentAggregertStatistikk_maskererKorttidOgLangtid_dersomAntallTilfellerErUnderFemIAlleKvartaler() {
        mockAvhengigheterForBarnehageMedIaRettigheter()

        every {
            mockSykefravarStatistikkVirksomhetRepository.hentUmaskertSykefravær(any(), any())
        } returns genererTestSykefravær(1)

        every {
            mockSykefraværStatistikkLandRepository.hentForKvartaler(any())
        } returns genererTestSykefravær(10)

        every {
            mockSykefraværStatistikkNæringskodeRepository.hentForBransje(any(), any())
        } returns genererTestSykefravær(30).map {
            SykefraværsstatistikkBransje(
                årstall = it.årstallOgKvartal.årstall,
                kvartal = it.årstallOgKvartal.kvartal,
                bransje = Bransje.BARNEHAGER,
                antallPersoner = it.antallPersoner,
                tapteDagsverk = it.dagsverkTeller,
                muligeDagsverk = it.dagsverkNevner
            )
        }

        every { mockSykefravarStatistikkVirksomhetRepository.hentSykefraværMedVarighet(any()) } returns
                listOf(
                    UmaskertSykefraværForEttKvartalMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL,
                        BigDecimal(10),
                        BigDecimal(0),
                        0,
                        Varighetskategori._20_UKER_TIL_39_UKER
                    ), UmaskertSykefraværForEttKvartalMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL,
                        BigDecimal(20),
                        BigDecimal(0),
                        0,
                        Varighetskategori._8_UKER_TIL_20_UKER
                    ), UmaskertSykefraværForEttKvartalMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL,
                        BigDecimal(5),
                        BigDecimal(0),
                        0,
                        Varighetskategori._8_DAGER_TIL_16_DAGER
                    ), UmaskertSykefraværForEttKvartalMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL,
                        BigDecimal(0),
                        BigDecimal(100),
                        3,
                        Varighetskategori.TOTAL
                    ), UmaskertSykefraværForEttKvartalMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1),
                        BigDecimal(10),
                        BigDecimal(0),
                        0,
                        Varighetskategori._20_UKER_TIL_39_UKER
                    ), UmaskertSykefraværForEttKvartalMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1),
                        BigDecimal(0),
                        BigDecimal(100),
                        4,
                        Varighetskategori.TOTAL
                    )

                )

        val resultat = serviceUnderTest.hentAggregertStatistikk(etOrgnr).getOrNull()!!

        resultat.prosentSiste4KvartalerKorttid shouldBeEqual emptyList()
        resultat.prosentSiste4KvartalerLangtid shouldBeEqual emptyList()
    }

    @Test
    fun hentAggregertStatistikk_returnererTapteOgMuligeDagsverk() {
        mockAvhengigheterForBarnehageMedIaRettigheter()

        every {
            mockSykefravarStatistikkVirksomhetRepository.hentUmaskertSykefravær(any(), any())
        } returns genererTestSykefravær(1)

        val respons = serviceUnderTest.hentAggregertStatistikk(etOrgnr).getOrNull()!!

        val antallMuligeDagsverk = respons.muligeDagsverkTotalt[0].verdi
        val antallTapteDagsverk = respons.tapteDagsverkTotalt[0].verdi

        assertThat(antallMuligeDagsverk).isEqualTo("400.0")
        assertThat(antallTapteDagsverk).isEqualTo("15.0")
    }

    @Test
    fun `Summen av langtid og kortidsfravær skal være lik totalfraværet`() {
        every { mockTilgangskontrollService.brukerRepresentererVirksomheten(any()) } returns true
        every { mockTilgangskontrollService.brukerHarIaRettigheterIVirksomheten(any()) } returns true
        every { mockEnhetsregisteretClient.hentUnderenhet(any()) } returns virksomhetUtenforBransjeprogrammet.right()

        val årstallOgKvartal = ÅrstallOgKvartal(2022, 1)

        every {
            mockSykefraværStatistikkNæringRepository.hentForKvartaler(any(), any())
        } returns
                listOf(
                    UmaskertSykefraværForEttKvartal(
                        årstallOgKvartal,
                        BigDecimal(68),
                        BigDecimal(1000),
                        20,
                    )
                )


        every { mockSykefraværStatistikkNæringMedVarighetRepository.hentSykefraværMedVarighetNæring(any()) } returns
                listOf(
                    UmaskertSykefraværForEttKvartalMedVarighet(
                        årstallOgKvartal,
                        BigDecimal(7),
                        BigDecimal(0),
                        20,
                        Varighetskategori._1_DAG_TIL_7_DAGER,
                    ),
                    UmaskertSykefraværForEttKvartalMedVarighet(
                        årstallOgKvartal,
                        BigDecimal(0),
                        BigDecimal(1000),
                        0,
                        Varighetskategori.TOTAL,
                    ),
                    UmaskertSykefraværForEttKvartalMedVarighet(
                        ÅrstallOgKvartal(1993, 2),
                        BigDecimal(799),
                        BigDecimal(0),
                        20,
                        Varighetskategori._1_DAG_TIL_7_DAGER,
                    ),
                    UmaskertSykefraværForEttKvartalMedVarighet(
                        ÅrstallOgKvartal(1993, 2),
                        BigDecimal(0),
                        BigDecimal(1000),
                        0,
                        Varighetskategori.TOTAL,
                    ),
                ) andThen
                listOf(
                    UmaskertSykefraværForEttKvartalMedVarighet(
                        årstallOgKvartal,
                        BigDecimal(61),
                        BigDecimal(0),
                        20,
                        Varighetskategori.MER_ENN_39_UKER
                    ),
                    UmaskertSykefraværForEttKvartalMedVarighet(
                        årstallOgKvartal,
                        BigDecimal(0),
                        BigDecimal(1000),
                        0,
                        Varighetskategori.TOTAL,
                    ),
                )

        val result = serviceUnderTest.hentAggregertStatistikk(etOrgnr).getOrNull()!!

        result.prosentSiste4KvartalerTotalt shouldBeEqual
                listOf(
                    StatistikkJson(
                        Statistikkategori.NÆRING,
                        "Offentlig administrasjon og forsvar, og trygdeordninger underlagt offentlig forvaltning",
                        "6.8",
                        20,
                        listOf(
                            årstallOgKvartal
                        )
                    )
                )

        result.prosentSiste4KvartalerKorttid shouldBeEqual
                listOf(
                    StatistikkJson(
                        Statistikkategori.NÆRING,
                        "Offentlig administrasjon og forsvar, og trygdeordninger underlagt offentlig forvaltning",
                        "0.7",
                        20,
                        listOf(
                            årstallOgKvartal
                        )
                    )
                )

        result.prosentSiste4KvartalerLangtid shouldBeEqual
                listOf(
                    StatistikkJson(
                        Statistikkategori.NÆRING,
                        "Offentlig administrasjon og forsvar, og trygdeordninger underlagt offentlig forvaltning",
                        "6.1",
                        20,
                        listOf(
                            årstallOgKvartal
                        )
                    )
                )
    }

    private fun mockAvhengigheterForBarnehageMedIaRettigheter() {
        every { mockTilgangskontrollService.brukerRepresentererVirksomheten(any()) } returns true
        every { mockEnhetsregisteretClient.hentUnderenhet(any()) } returns enBarnehage.right()
        every { mockTilgangskontrollService.brukerHarIaRettigheterIVirksomheten(any()) } returns true
    }

    private fun genererTestSykefravær(offset: Int): List<UmaskertSykefraværForEttKvartal> {
        return listOf(
            umaskertSykefravær(ÅrstallOgKvartal(2022, 1), 1.1 + offset, 10),
            umaskertSykefravær(ÅrstallOgKvartal(2021, 4), 2.2 + offset, 10),
            umaskertSykefravær(ÅrstallOgKvartal(2021, 3), 3.3 + offset, 10),
            umaskertSykefravær(ÅrstallOgKvartal(2021, 2), 4.4 + offset, 10),
            umaskertSykefravær(ÅrstallOgKvartal(2021, 1), 5.5 + offset, 10),
            umaskertSykefravær(ÅrstallOgKvartal(2020, 4), 6.6 + offset, 10),
            umaskertSykefravær(ÅrstallOgKvartal(2020, 3), 7.7 + offset, 10)
        )
    }

    companion object {
        private fun umaskertSykefravær(
            årstallOgKvartal: ÅrstallOgKvartal, tapteDagsverk: Double, antallPersoner: Int
        ) = UmaskertSykefraværForEttKvartal(
            årstallOgKvartal, BigDecimal(tapteDagsverk), BigDecimal(100), antallPersoner
        )


        private val etOrgnr = Orgnr("999999999")
        private val etAnnetOrgnr = Orgnr("1111111111")

        private val enBarnehage = Underenhet.Næringsdrivende(
            orgnr = etOrgnr,
            navn = "En Barnehage",
            næringskode = Næringskode("88911"),
            antallAnsatte = 10,
            overordnetEnhetOrgnr = etAnnetOrgnr
        )

        private val virksomhetUtenforBransjeprogrammet = Underenhet.Næringsdrivende(
            orgnr = etOrgnr,
            overordnetEnhetOrgnr = etAnnetOrgnr,
            navn = "Virksomhet utenfor bransjeprogrammet",
            antallAnsatte = 20,
            næringskode = Næringskode("84300")
        )
    }
}
