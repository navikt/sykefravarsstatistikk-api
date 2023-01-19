package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.SISTE_PUBLISERTE_KVARTAL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.ArbeidsmiljøportalenBransje.BARNEHAGER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.BRANSJE;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.LAND;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.NÆRING;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.VIRKSOMHET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api.PubliseringsdatoerService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.GraderingRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.VarighetRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregertStatistikkServiceTest {

  private final BransjeEllerNæring enBransje =
      new BransjeEllerNæring(new Bransje(BARNEHAGER, "En bransje", "88911"));
  private final Orgnr etOrgnr = new Orgnr("999999999");
  private final Underenhet enBarnehage =
      Underenhet.builder()
          .orgnr(etOrgnr)
          .navn("En Barnehage")
          .næringskode(new Næringskode5Siffer("88911", "Barnehage"))
          .antallAnsatte(10)
          .overordnetEnhetOrgnr(new Orgnr("1111111111"))
          .build();

  private AggregertStatistikkService serviceUnderTest;

  @Mock
  private SykefraværRepository mockSykefraværRepository;
  @Mock
  private GraderingRepository mockGraderingRepository;
  @Mock
  private TilgangskontrollService mockTilgangskontrollService;
  @Mock
  private EnhetsregisteretClient mockEnhetsregisteretClient;
  @Mock
  private BransjeEllerNæringService mockBransjeEllerNæringService;
  @Mock
  private VarighetRepository mockVarighetRepository;
  @Mock
  private PubliseringsdatoerService publiseringsdatoerService;

  @BeforeEach
  public void setUp() {
    when(publiseringsdatoerService.hentSistePubliserteKvartal())
        .thenReturn(SISTE_PUBLISERTE_KVARTAL);

    serviceUnderTest =
        new AggregertStatistikkService(
            mockSykefraværRepository,
            mockGraderingRepository,
            mockVarighetRepository,
            mockBransjeEllerNæringService,
            mockTilgangskontrollService,
            mockEnhetsregisteretClient,
            publiseringsdatoerService);
  }

  @AfterEach
  public void tearDown() {
    reset(
        mockSykefraværRepository,
        mockGraderingRepository,
        mockVarighetRepository,
        mockEnhetsregisteretClient,
        mockBransjeEllerNæringService,
        mockTilgangskontrollService);
  }

  @Test
  void hentAggregertHistorikk_kræsjerIkkeVedManglendeData() {
    when(mockTilgangskontrollService.brukerRepresentererVirksomheten(any())).thenReturn(true);
    when(mockEnhetsregisteretClient.hentInformasjonOmUnderenhet(any())).thenReturn(enBarnehage);
    when(mockTilgangskontrollService.brukerHarIaRettigheterIVirksomheten(any())).thenReturn(true);
    when(mockBransjeEllerNæringService.finnBransje(any())).thenReturn(enBransje);

    // Helt tomt resultat skal ikke kræsje
    when(mockSykefraværRepository.hentUmaskertSykefraværAlleKategorier(any(), any()))
        .thenReturn(new Sykefraværsdata(Map.of()));
    when(mockGraderingRepository.hentGradertSykefraværAlleKategorier(any()))
        .thenReturn(new Sykefraværsdata(Map.of()));
    assertThat(serviceUnderTest.hentAggregertStatistikk(etOrgnr).get())
        .isEqualTo(new AggregertStatistikkDto());

    // Resultat med statistikkategri og tomme lister skal heller ikke kræsje
    when(mockSykefraværRepository.hentUmaskertSykefraværAlleKategorier(any(), any()))
        .thenReturn(
            new Sykefraværsdata(
                Map.of(
                    VIRKSOMHET, List.of(),
                    LAND, List.of(),
                    NÆRING, List.of(),
                    BRANSJE, List.of())));
    when(mockGraderingRepository.hentGradertSykefraværAlleKategorier(any()))
        .thenReturn(
            new Sykefraværsdata(
                Map.of(
                    VIRKSOMHET, List.of(),
                    NÆRING, List.of(),
                    BRANSJE, List.of())));
    assertThat(serviceUnderTest.hentAggregertStatistikk(etOrgnr).get())
        .isEqualTo(new AggregertStatistikkDto());
  }

  @Test
  void hentAggregertHistorikk_henterAltSykefraværDersomBrukerHarIaRettigheter() {
    mockAvhengigheterForBarnehageMedIaRettigheter();

    when(mockSykefraværRepository.hentUmaskertSykefraværAlleKategorier(any(), any()))
        .thenReturn(
            new Sykefraværsdata(
                Map.of(
                    VIRKSOMHET, genererTestSykefravær(1),
                    LAND, genererTestSykefravær(10),
                    NÆRING, genererTestSykefravær(20),
                    BRANSJE, genererTestSykefravær(30))));
    when(mockGraderingRepository.hentGradertSykefraværAlleKategorier(any()))
        .thenReturn(
            new Sykefraværsdata(
                Map.of(
                    VIRKSOMHET, genererTestSykefravær(50),
                    NÆRING, genererTestSykefravær(60),
                    BRANSJE, genererTestSykefravær(70))));

    List<Statistikkategori> forventedeProsenttyper = List.of(VIRKSOMHET, BRANSJE, LAND);

    List<Statistikkategori> forventedeGraderingstyper = List.of(VIRKSOMHET, BRANSJE);

    List<Statistikkategori> forventedeTrendtyper = List.of(BRANSJE);

    List<Statistikkategori> prosentstatistikk =
        serviceUnderTest
            .hentAggregertStatistikk(etOrgnr)
            .get()
            .prosentSiste4KvartalerTotalt
            .stream()
            .map(StatistikkDto::getStatistikkategori)
            .collect(Collectors.toList());
    List<Statistikkategori> gradertProsentstatistikk =
        serviceUnderTest
            .hentAggregertStatistikk(etOrgnr)
            .get()
            .prosentSiste4KvartalerGradert
            .stream()
            .map(StatistikkDto::getStatistikkategori)
            .collect(Collectors.toList());

    List<Statistikkategori> trendstatistikk =
        serviceUnderTest.hentAggregertStatistikk(etOrgnr).get().trendTotalt.stream()
            .map(StatistikkDto::getStatistikkategori)
            .collect(Collectors.toList());

    assertThat(prosentstatistikk).isEqualTo(forventedeProsenttyper);
    assertThat(gradertProsentstatistikk).isEqualTo(forventedeGraderingstyper);
    assertThat(trendstatistikk).isEqualTo(forventedeTrendtyper);
  }

  @Test
  public void
  hentAggregertStatistikk_regnerUtLangtidsfravær_dersomAntallPersonerErOverEllerLikFem() {
    mockAvhengigheterForBarnehageMedIaRettigheter();
    when(mockSykefraværRepository.hentUmaskertSykefraværAlleKategorier(any(), any()))
        .thenReturn(new Sykefraværsdata(Map.of(VIRKSOMHET, genererTestSykefravær(1))));
    when(mockGraderingRepository.hentGradertSykefraværAlleKategorier(any()))
        .thenReturn(new Sykefraværsdata(Map.of(VIRKSOMHET, genererTestSykefravær(50))));
    when(mockVarighetRepository.hentUmaskertSykefraværMedVarighetAlleKategorier(any()))
        .thenReturn(
            Map.of(
                VIRKSOMHET,
                List.of(
                    fraværMedVarighet(
                        SISTE_PUBLISERTE_KVARTAL, 10, 0, 0, Varighetskategori._20_UKER_TIL_39_UKER),
                    fraværMedVarighet(
                        SISTE_PUBLISERTE_KVARTAL, 40, 0, 0, Varighetskategori._17_DAGER_TIL_8_UKER),
                    fraværMedVarighet(
                        SISTE_PUBLISERTE_KVARTAL, 20, 0, 0, Varighetskategori._8_UKER_TIL_20_UKER),
                    fraværMedVarighet(
                        SISTE_PUBLISERTE_KVARTAL, 5, 0, 0, Varighetskategori._8_DAGER_TIL_16_DAGER),
                    fraværMedVarighet(
                        SISTE_PUBLISERTE_KVARTAL, 0, 100, 200, Varighetskategori.TOTAL),
                    fraværMedVarighet(
                        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1),
                        10,
                        0,
                        0,
                        Varighetskategori._20_UKER_TIL_39_UKER),
                    fraværMedVarighet(
                        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1),
                        0,
                        100,
                        5,
                        Varighetskategori.TOTAL))));

    StatistikkDto forventet =
        new StatistikkDto(
            VIRKSOMHET,
            "En Barnehage",
            "40.0",
            200,
            List.of(SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1), SISTE_PUBLISERTE_KVARTAL));
    assertThat(
        serviceUnderTest
            .hentAggregertStatistikk(etOrgnr)
            .get()
            .prosentSiste4KvartalerLangtid
            .get(0))
        .isEqualTo(forventet);
  }

  @Test
  public void
  hentAggregertStatistikk_returnererKorttidsfravær_dersomAntallPersonerErOverEllerLikFem() {
    mockAvhengigheterForBarnehageMedIaRettigheter();
    when(mockSykefraværRepository.hentUmaskertSykefraværAlleKategorier(any(), any()))
        .thenReturn(new Sykefraværsdata(Map.of(VIRKSOMHET, genererTestSykefravær(1))));
    when(mockGraderingRepository.hentGradertSykefraværAlleKategorier(any()))
        .thenReturn(new Sykefraværsdata(Map.of(VIRKSOMHET, genererTestSykefravær(50))));
    when(mockVarighetRepository.hentUmaskertSykefraværMedVarighetAlleKategorier(any()))
        .thenReturn(
            Map.of(
                VIRKSOMHET,
                List.of(
                    fraværMedVarighet(
                        SISTE_PUBLISERTE_KVARTAL, 40, 0, 0, Varighetskategori._1_DAG_TIL_7_DAGER),
                    fraværMedVarighet(
                        SISTE_PUBLISERTE_KVARTAL, 5, 0, 0, Varighetskategori._8_DAGER_TIL_16_DAGER),
                    fraværMedVarighet(
                        SISTE_PUBLISERTE_KVARTAL, 0, 100, 100, Varighetskategori.TOTAL),
                    fraværMedVarighet(
                        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1),
                        10,
                        0,
                        0,
                        Varighetskategori._8_DAGER_TIL_16_DAGER),
                    fraværMedVarighet(
                        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1),
                        0,
                        100,
                        0,
                        Varighetskategori.TOTAL))));

    StatistikkDto forventet =
        new StatistikkDto(
            VIRKSOMHET,
            "En Barnehage",
            "27.5",
            100,
            List.of(SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1), SISTE_PUBLISERTE_KVARTAL));
    assertThat(
        serviceUnderTest
            .hentAggregertStatistikk(etOrgnr)
            .get()
            .prosentSiste4KvartalerKorttid
            .get(0))
        .isEqualTo(forventet);
  }

  @Test
  public void
  hentAggregertStatistikk_maskererKorttidOgLangtid_dersomAntallTilfellerErUnderFemIAlleKvartaler() {
    mockAvhengigheterForBarnehageMedIaRettigheter();
    when(mockSykefraværRepository.hentUmaskertSykefraværAlleKategorier(any(), any()))
        .thenReturn(
            new Sykefraværsdata(
                Map.of(
                    VIRKSOMHET, genererTestSykefravær(1),
                    LAND, genererTestSykefravær(10),
                    NÆRING, genererTestSykefravær(20),
                    BRANSJE, genererTestSykefravær(30))));
    when(mockGraderingRepository.hentGradertSykefraværAlleKategorier(any()))
        .thenReturn(
            new Sykefraværsdata(
                Map.of(
                    VIRKSOMHET, genererTestSykefravær(50),
                    NÆRING, genererTestSykefravær(60),
                    BRANSJE, genererTestSykefravær(70))));
    when(mockVarighetRepository.hentUmaskertSykefraværMedVarighetAlleKategorier(any()))
        .thenReturn(
            Map.of(
                VIRKSOMHET,
                List.of(
                    fraværMedVarighet(
                        SISTE_PUBLISERTE_KVARTAL, 10, 0, 0, Varighetskategori._20_UKER_TIL_39_UKER),
                    fraværMedVarighet(
                        SISTE_PUBLISERTE_KVARTAL, 20, 0, 0, Varighetskategori._8_UKER_TIL_20_UKER),
                    fraværMedVarighet(
                        SISTE_PUBLISERTE_KVARTAL, 5, 0, 0, Varighetskategori._8_DAGER_TIL_16_DAGER),
                    fraværMedVarighet(SISTE_PUBLISERTE_KVARTAL, 0, 100, 3, Varighetskategori.TOTAL),
                    fraværMedVarighet(
                        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1),
                        10,
                        0,
                        0,
                        Varighetskategori._20_UKER_TIL_39_UKER),
                    fraværMedVarighet(
                        SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1),
                        0,
                        100,
                        4,
                        Varighetskategori.TOTAL))));

    assertThat(
        serviceUnderTest.hentAggregertStatistikk(etOrgnr).get().prosentSiste4KvartalerKorttid)
        .isEqualTo(List.of());
    assertThat(
        serviceUnderTest.hentAggregertStatistikk(etOrgnr).get().prosentSiste4KvartalerLangtid)
        .isEqualTo(List.of());
  }

  @Test
  public void hentAggregertStatistikk_returnererTapteOgMuligeDagsverk() {

    mockAvhengigheterForBarnehageMedIaRettigheter();

    when(mockSykefraværRepository.hentUmaskertSykefraværAlleKategorier(any(), any()))
        .thenReturn(new Sykefraværsdata(Map.of(VIRKSOMHET, genererTestSykefravær(1))));
    when(mockGraderingRepository.hentGradertSykefraværAlleKategorier(any()))
        .thenReturn(new Sykefraværsdata(Map.of()));
    when(mockVarighetRepository.hentUmaskertSykefraværMedVarighetAlleKategorier(any()))
        .thenReturn(Map.of());
    AggregertStatistikkDto respons = serviceUnderTest.hentAggregertStatistikk(etOrgnr).get();
    String antallMuligeDagsverk = respons.muligeDagsverkTotalt.get(0).getVerdi();
    String antallTapteDagsverk = respons.tapteDagsverkTotalt.get(0).getVerdi();

    assertThat(antallMuligeDagsverk).isEqualTo("400.0");
    assertThat(antallTapteDagsverk).isEqualTo("15.0");
  }

  private void mockAvhengigheterForBarnehageMedIaRettigheter() {
    when(mockTilgangskontrollService.brukerRepresentererVirksomheten(any())).thenReturn(true);
    when(mockEnhetsregisteretClient.hentInformasjonOmUnderenhet(any())).thenReturn(enBarnehage);
    when(mockTilgangskontrollService.brukerHarIaRettigheterIVirksomheten(any())).thenReturn(true);
    when(mockBransjeEllerNæringService.finnBransje(any())).thenReturn(enBransje);
  }

  private List<UmaskertSykefraværForEttKvartal> genererTestSykefravær(int offset) {
    return Arrays.asList(
        umaskertSykefravær(new ÅrstallOgKvartal(2022, 1), 1.1 + offset, 10),
        umaskertSykefravær(new ÅrstallOgKvartal(2021, 4), 2.2 + offset, 10),
        umaskertSykefravær(new ÅrstallOgKvartal(2021, 3), 3.3 + offset, 10),
        umaskertSykefravær(new ÅrstallOgKvartal(2021, 2), 4.4 + offset, 10),
        umaskertSykefravær(new ÅrstallOgKvartal(2021, 1), 5.5 + offset, 10),
        umaskertSykefravær(new ÅrstallOgKvartal(2020, 4), 6.6 + offset, 10),
        umaskertSykefravær(new ÅrstallOgKvartal(2020, 3), 7.7 + offset, 10));
  }

  private static UmaskertSykefraværForEttKvartal umaskertSykefravær(
      ÅrstallOgKvartal årstallOgKvartal, double tapteDagsverk, int antallPersoner) {
    return new UmaskertSykefraværForEttKvartal(
        årstallOgKvartal, new BigDecimal(tapteDagsverk), new BigDecimal(100), antallPersoner);
  }

  private static UmaskertSykefraværForEttKvartalMedVarighet fraværMedVarighet(
      ÅrstallOgKvartal årstallOgKvartal,
      int tapteDagsverk,
      int muligeDagsverk,
      int antallPersoner,
      Varighetskategori varighetskategori) {
    return new UmaskertSykefraværForEttKvartalMedVarighet(
        årstallOgKvartal,
        new BigDecimal(tapteDagsverk),
        new BigDecimal(muligeDagsverk),
        antallPersoner,
        varighetskategori);
  }
}
