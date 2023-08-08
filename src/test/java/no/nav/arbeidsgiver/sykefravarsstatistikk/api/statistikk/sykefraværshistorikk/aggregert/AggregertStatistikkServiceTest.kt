package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert

import arrow.core.right
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.ArbeidsmiljøportalenBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.BransjeEllerNæring
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.BransjeEllerNæringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.enhetsregisteret.EnhetsregisteretClient
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.PubliseringsdatoerService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering.AggregertStatistikkService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Varighetskategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.UmaskertSykefraværForEttKvartalMedVarighet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.GraderingRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.VarighetRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.TilgangskontrollService
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.stream.Collectors

@ExtendWith(MockitoExtension::class)
internal class AggregertStatistikkServiceTest {

    private lateinit var serviceUnderTest: AggregertStatistikkService

    @Mock
    private lateinit var mockSykefraværRepository: SykefraværRepository

    @Mock
    private lateinit var mockGraderingRepository: GraderingRepository

    @Mock
    private lateinit var mockTilgangskontrollService: TilgangskontrollService

    @Mock
    private lateinit var mockEnhetsregisteretClient: EnhetsregisteretClient

    @Mock
    private lateinit var mockBransjeEllerNæringService: BransjeEllerNæringService

    @Mock
    private lateinit var mockVarighetRepository: VarighetRepository

    @Mock
    private lateinit var publiseringsdatoerService: PubliseringsdatoerService

    @BeforeEach
    fun setUp() {
        whenever(publiseringsdatoerService.hentSistePubliserteKvartal()).thenReturn(TestUtils.SISTE_PUBLISERTE_KVARTAL)
        serviceUnderTest = AggregertStatistikkService(
            mockSykefraværRepository,
            mockGraderingRepository,
            mockVarighetRepository,
            mockBransjeEllerNæringService,
            mockTilgangskontrollService,
            mockEnhetsregisteretClient,
            publiseringsdatoerService
        )
    }

    @AfterEach
    fun tearDown() {
        Mockito.reset(
            mockSykefraværRepository,
            mockGraderingRepository,
            mockVarighetRepository,
            mockEnhetsregisteretClient,
            mockBransjeEllerNæringService,
            mockTilgangskontrollService
        )
    }

    @Test
    fun hentAggregertHistorikk_kræsjerIkkeVedManglendeData() {
        whenever(mockTilgangskontrollService.brukerRepresentererVirksomheten(any())).thenReturn(true)
        whenever(mockEnhetsregisteretClient.hentUnderenhet(any())).thenReturn(enBarnehage.right())
        whenever(mockTilgangskontrollService.brukerHarIaRettigheterIVirksomheten(any())).thenReturn(true)
        whenever(mockBransjeEllerNæringService.finnBransje(any())).thenReturn(barnehager)

        // Helt tomt resultat skal ikke kræsje
        whenever(
            mockSykefraværRepository.hentTotaltSykefraværAlleKategorier(
                any(), any()
            )
        ).thenReturn(Sykefraværsdata(mutableMapOf()))
        whenever(mockGraderingRepository.hentGradertSykefraværAlleKategorier(any())).thenReturn(
            Sykefraværsdata(
                mutableMapOf()
            )
        )
        assertThat(serviceUnderTest.hentAggregertStatistikk(etOrgnr).getOrNull()).isEqualTo(AggregertStatistikkDto())

        // Resultat med statistikkategori og tomme lister skal heller ikke kræsje
        whenever(
            mockSykefraværRepository.hentTotaltSykefraværAlleKategorier(
                any(), any()
            )
        ).thenReturn(
            Sykefraværsdata(
                mutableMapOf(
                    Statistikkategori.VIRKSOMHET to listOf(),
                    Statistikkategori.LAND to listOf(),
                    Statistikkategori.NÆRING to listOf(),
                    Statistikkategori.BRANSJE to listOf()
                )
            )
        )
        whenever(mockGraderingRepository.hentGradertSykefraværAlleKategorier(any())).thenReturn(
            Sykefraværsdata(
                mutableMapOf(
                    Statistikkategori.VIRKSOMHET to listOf(),
                    Statistikkategori.NÆRING to listOf(),
                    Statistikkategori.BRANSJE to listOf()
                )
            )
        )
        assertThat(serviceUnderTest.hentAggregertStatistikk(etOrgnr).getOrNull()).isEqualTo(AggregertStatistikkDto())
    }

    @Test
    fun hentAggregertHistorikk_henterAltSykefraværDersomBrukerHarIaRettigheter() {
        mockAvhengigheterForBarnehageMedIaRettigheter()
        whenever(
            mockSykefraværRepository.hentTotaltSykefraværAlleKategorier(
                any(), any()
            )
        ).thenReturn(
            Sykefraværsdata(
                mutableMapOf(
                    Statistikkategori.VIRKSOMHET to genererTestSykefravær(1),
                    Statistikkategori.LAND to genererTestSykefravær(10),
                    Statistikkategori.NÆRING to genererTestSykefravær(20),
                    Statistikkategori.BRANSJE to genererTestSykefravær(30)
                )
            )
        )
        whenever(mockGraderingRepository.hentGradertSykefraværAlleKategorier(any())).thenReturn(
            Sykefraværsdata(
                mutableMapOf(
                    Statistikkategori.VIRKSOMHET to genererTestSykefravær(50),
                    Statistikkategori.NÆRING to genererTestSykefravær(60),
                    Statistikkategori.BRANSJE to genererTestSykefravær(70)
                )
            )
        )
        val forventedeProsenttyper =
            listOf(Statistikkategori.VIRKSOMHET, Statistikkategori.BRANSJE, Statistikkategori.LAND)
        val forventedeGraderingstyper = listOf(Statistikkategori.VIRKSOMHET, Statistikkategori.BRANSJE)
        val forventedeTrendtyper = listOf(Statistikkategori.BRANSJE)
        val prosentstatistikk =
            serviceUnderTest.hentAggregertStatistikk(etOrgnr).getOrNull()?.prosentSiste4KvartalerTotalt?.stream()
                ?.map(StatistikkDto::statistikkategori)?.collect(Collectors.toList())
        val gradertProsentstatistikk =
            serviceUnderTest.hentAggregertStatistikk(etOrgnr).getOrNull()?.prosentSiste4KvartalerGradert?.stream()
                ?.map(StatistikkDto::statistikkategori)?.collect(Collectors.toList())
        val trendstatistikk = serviceUnderTest.hentAggregertStatistikk(etOrgnr).getOrNull()?.trendTotalt?.stream()
            ?.map(StatistikkDto::statistikkategori)?.collect(Collectors.toList())
        assertThat(prosentstatistikk).isEqualTo(forventedeProsenttyper)
        assertThat(gradertProsentstatistikk).isEqualTo(forventedeGraderingstyper)
        assertThat(trendstatistikk).isEqualTo(forventedeTrendtyper)
    }

    @Test
    fun hentAggregertStatistikk_regnerUtLangtidsfravær_dersomAntallPersonerErOverEllerLikFem() {
        mockAvhengigheterForBarnehageMedIaRettigheter()

        whenever(
            mockSykefraværRepository.hentTotaltSykefraværAlleKategorier(
                any(), any()
            )
        ).thenReturn(Sykefraværsdata(mutableMapOf(Statistikkategori.VIRKSOMHET to genererTestSykefravær(1))))
        whenever(mockGraderingRepository.hentGradertSykefraværAlleKategorier(any())).thenReturn(
            Sykefraværsdata(
                mutableMapOf(Statistikkategori.VIRKSOMHET to genererTestSykefravær(50))
            )
        )
        whenever(
            mockVarighetRepository.hentUmaskertSykefraværMedVarighetAlleKategorier(any())
        ).thenReturn(
            mapOf(
                Statistikkategori.VIRKSOMHET to listOf(
                    fraværMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL, 10, 0, 0, Varighetskategori._20_UKER_TIL_39_UKER
                    ), fraværMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL, 40, 0, 0, Varighetskategori._17_DAGER_TIL_8_UKER
                    ), fraværMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL, 20, 0, 0, Varighetskategori._8_UKER_TIL_20_UKER
                    ), fraværMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL, 5, 0, 0, Varighetskategori._8_DAGER_TIL_16_DAGER
                    ), fraværMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL, 0, 100, 200, Varighetskategori.TOTAL
                    ), fraværMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1),
                        10,
                        0,
                        0,
                        Varighetskategori._20_UKER_TIL_39_UKER
                    ), fraværMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1), 0, 100, 5, Varighetskategori.TOTAL
                    )
                )
            )
        )
        val forventet = StatistikkDto(
            Statistikkategori.VIRKSOMHET,
            "En Barnehage",
            "40.0",
            200,
            listOf(TestUtils.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1), TestUtils.SISTE_PUBLISERTE_KVARTAL)
        )
        assertThat(
            serviceUnderTest.hentAggregertStatistikk(etOrgnr).getOrNull()?.prosentSiste4KvartalerLangtid!![0]
        ).isEqualTo(forventet)
    }

    @Test
    fun hentAggregertStatistikk_returnererKorttidsfravær_dersomAntallPersonerErOverEllerLikFem() {
        mockAvhengigheterForBarnehageMedIaRettigheter()
        whenever(
            mockSykefraværRepository.hentTotaltSykefraværAlleKategorier(
                any(), any()
            )
        ).thenReturn(Sykefraværsdata(mutableMapOf(Statistikkategori.VIRKSOMHET to genererTestSykefravær(1))))
        whenever(mockGraderingRepository.hentGradertSykefraværAlleKategorier(any())).thenReturn(
            Sykefraværsdata(
                mutableMapOf(Statistikkategori.VIRKSOMHET to genererTestSykefravær(50))
            )
        )
        whenever(
            mockVarighetRepository.hentUmaskertSykefraværMedVarighetAlleKategorier(any())
        ).thenReturn(
            mapOf(
                Statistikkategori.VIRKSOMHET to listOf(
                    fraværMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL, 40, 0, 0, Varighetskategori._1_DAG_TIL_7_DAGER
                    ), fraværMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL, 5, 0, 0, Varighetskategori._8_DAGER_TIL_16_DAGER
                    ), fraværMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL, 0, 100, 100, Varighetskategori.TOTAL
                    ), fraværMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1),
                        10,
                        0,
                        0,
                        Varighetskategori._8_DAGER_TIL_16_DAGER
                    ), fraværMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1), 0, 100, 0, Varighetskategori.TOTAL
                    )
                )
            )
        )
        val forventet = StatistikkDto(
            Statistikkategori.VIRKSOMHET,
            "En Barnehage",
            "27.5",
            100,
            listOf(TestUtils.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1), TestUtils.SISTE_PUBLISERTE_KVARTAL)
        )
        assertThat(
            serviceUnderTest.hentAggregertStatistikk(etOrgnr).getOrNull()?.prosentSiste4KvartalerKorttid!![0]
        ).isEqualTo(forventet)
    }

    @Test
    fun hentAggregertStatistikk_maskererKorttidOgLangtid_dersomAntallTilfellerErUnderFemIAlleKvartaler() {
        mockAvhengigheterForBarnehageMedIaRettigheter()
        whenever(
            mockSykefraværRepository.hentTotaltSykefraværAlleKategorier(
                any(), any()
            )
        ).thenReturn(
            Sykefraværsdata(
                mutableMapOf(
                    Statistikkategori.VIRKSOMHET to genererTestSykefravær(1),
                    Statistikkategori.LAND to genererTestSykefravær(10),
                    Statistikkategori.NÆRING to genererTestSykefravær(20),
                    Statistikkategori.BRANSJE to genererTestSykefravær(30)
                )
            )
        )
        whenever(mockGraderingRepository.hentGradertSykefraværAlleKategorier(any())).thenReturn(
            Sykefraværsdata(
                mutableMapOf(
                    Statistikkategori.VIRKSOMHET to genererTestSykefravær(50),
                    Statistikkategori.NÆRING to genererTestSykefravær(60),
                    Statistikkategori.BRANSJE to genererTestSykefravær(70)
                )
            )
        )
        whenever(
            mockVarighetRepository.hentUmaskertSykefraværMedVarighetAlleKategorier(any())
        ).thenReturn(
            mapOf(
                Statistikkategori.VIRKSOMHET to listOf(
                    fraværMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL, 10, 0, 0, Varighetskategori._20_UKER_TIL_39_UKER
                    ), fraværMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL, 20, 0, 0, Varighetskategori._8_UKER_TIL_20_UKER
                    ), fraværMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL, 5, 0, 0, Varighetskategori._8_DAGER_TIL_16_DAGER
                    ), fraværMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL, 0, 100, 3, Varighetskategori.TOTAL
                    ), fraværMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1),
                        10,
                        0,
                        0,
                        Varighetskategori._20_UKER_TIL_39_UKER
                    ), fraværMedVarighet(
                        TestUtils.SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1), 0, 100, 4, Varighetskategori.TOTAL
                    )
                )
            )
        )
        assertThat(
            serviceUnderTest.hentAggregertStatistikk(etOrgnr).getOrNull()?.prosentSiste4KvartalerKorttid
        ).isEqualTo(listOf<Any>())
        assertThat(
            serviceUnderTest.hentAggregertStatistikk(etOrgnr).getOrNull()?.prosentSiste4KvartalerLangtid
        ).isEqualTo(listOf<Any>())
    }

    @Test
    fun hentAggregertStatistikk_returnererTapteOgMuligeDagsverk() {
        mockAvhengigheterForBarnehageMedIaRettigheter()
        whenever(
            mockSykefraværRepository.hentTotaltSykefraværAlleKategorier(
                any(), any()
            )
        ).thenReturn(Sykefraværsdata(mutableMapOf(Statistikkategori.VIRKSOMHET to genererTestSykefravær(1))))
        whenever(mockGraderingRepository.hentGradertSykefraværAlleKategorier(any())).thenReturn(
            Sykefraværsdata(
                mutableMapOf()
            )
        )
        whenever(
            mockVarighetRepository.hentUmaskertSykefraværMedVarighetAlleKategorier(any())
        ).thenReturn(mapOf())
        val respons = serviceUnderTest.hentAggregertStatistikk(etOrgnr).getOrNull()
        val antallMuligeDagsverk = respons?.muligeDagsverkTotalt?.get(0)?.verdi
        val antallTapteDagsverk = respons?.tapteDagsverkTotalt?.get(0)?.verdi
        assertThat(antallMuligeDagsverk).isEqualTo("400.0")
        assertThat(antallTapteDagsverk).isEqualTo("15.0")
    }

    @Test
    fun `Summen av langtid og kortidsfravær skal være lik totalfraværet`() {
        whenever(mockTilgangskontrollService.brukerRepresentererVirksomheten(any())).thenReturn(true)
        whenever(mockTilgangskontrollService.brukerHarIaRettigheterIVirksomheten(any())).thenReturn(true)
        whenever(mockEnhetsregisteretClient.hentUnderenhet(any())).thenReturn(virksomhetUtenforBransjeprogrammet.right())
        whenever(mockGraderingRepository.hentGradertSykefraværAlleKategorier(any())).thenReturn(
            Sykefraværsdata(
            mutableMapOf()
        )
        )
        whenever(mockBransjeEllerNæringService.finnBransje(any()))
            .thenReturn(BransjeEllerNæring(Næring("84", "Et langt navn")))

        val årstallOgKvartal = ÅrstallOgKvartal(2022, 1)
        whenever(mockSykefraværRepository.hentTotaltSykefraværAlleKategorier(any(), any())).thenReturn(
                Sykefraværsdata(
                    mutableMapOf(
                        Statistikkategori.NÆRING to listOf(
                            UmaskertSykefraværForEttKvartal(
                                årstallOgKvartal,
                                BigDecimal(68),
                                BigDecimal(1000),
                                20,
                            )
                        )
                    )
                )
            )

        whenever(mockVarighetRepository.hentUmaskertSykefraværMedVarighetAlleKategorier(any()))
            // Korttidsfravær 0.7%
            .thenReturn(
                mutableMapOf(
                    Statistikkategori.NÆRING to listOf(
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
                    )
                )
            )
            // Langtidsfravær 6.1%
            .thenReturn(
                mutableMapOf(
                    Statistikkategori.NÆRING to listOf(
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
                )
            )

        val result = serviceUnderTest.hentAggregertStatistikk(etOrgnr).getOrNull()

        assertThat(result?.prosentSiste4KvartalerTotalt).isEqualTo(
            listOf(
                StatistikkDto(
                    Statistikkategori.NÆRING, "Et langt navn", "6.8", 20, listOf(
                        årstallOgKvartal
                    )
                )
            )
        )
        assertThat(result?.prosentSiste4KvartalerKorttid).isEqualTo(
            listOf(
                StatistikkDto(
                    Statistikkategori.NÆRING, "Et langt navn", "0.7", 20, listOf(
                        årstallOgKvartal
                    )
                )
            )
        )
        assertThat(result?.prosentSiste4KvartalerLangtid).isEqualTo(
            listOf(
                StatistikkDto(
                    Statistikkategori.NÆRING, "Et langt navn", "6.1", 20, listOf(
                        årstallOgKvartal
                    )
                )
            )
        )

        // Sjekk at langtidsfraværet da er 6,8% (i henvendelsen var tallet 4,7%)
    }

    private fun mockAvhengigheterForBarnehageMedIaRettigheter() {
        whenever(mockTilgangskontrollService.brukerRepresentererVirksomheten(any())).thenReturn(true)
        whenever(mockEnhetsregisteretClient.hentUnderenhet(any())).thenReturn(enBarnehage.right())
        whenever(mockTilgangskontrollService.brukerHarIaRettigheterIVirksomheten(any())).thenReturn(true)
        whenever(mockBransjeEllerNæringService.finnBransje(any())).thenReturn(barnehager)
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
        ): UmaskertSykefraværForEttKvartal {
            return UmaskertSykefraværForEttKvartal(
                årstallOgKvartal, BigDecimal(tapteDagsverk), BigDecimal(100), antallPersoner
            )
        }

        private fun fraværMedVarighet(
            årstallOgKvartal: ÅrstallOgKvartal,
            tapteDagsverk: Int,
            muligeDagsverk: Int,
            antallPersoner: Int,
            varighetskategori: Varighetskategori
        ): UmaskertSykefraværForEttKvartalMedVarighet {
            return UmaskertSykefraværForEttKvartalMedVarighet(
                årstallOgKvartal,
                BigDecimal(tapteDagsverk),
                BigDecimal(muligeDagsverk),
                antallPersoner,
                varighetskategori
            )
        }
    }

    private val barnehager = BransjeEllerNæring(Bransje(ArbeidsmiljøportalenBransje.BARNEHAGER, "En bransje", "88911"))
    private val etOrgnr = Orgnr("999999999")
    private val etAnnetOrgnr = Orgnr("1111111111")

    private val enBarnehage = Underenhet.Næringsdrivende(
        orgnr = etOrgnr,
        navn = "En Barnehage",
        næringskode = Næringskode5Siffer("88911", "Barnehage"),
        antallAnsatte = 10,
        overordnetEnhetOrgnr = etAnnetOrgnr
    )

    private val virksomhetUtenforBransjeprogrammet = Underenhet.Næringsdrivende(
        orgnr = etOrgnr,
        overordnetEnhetOrgnr = etAnnetOrgnr,
        navn = "Virksomhet utenfor bransjeprogrammet",
        antallAnsatte = 20,
        næringskode = Næringskode5Siffer("84300", "Næring")
    )
}