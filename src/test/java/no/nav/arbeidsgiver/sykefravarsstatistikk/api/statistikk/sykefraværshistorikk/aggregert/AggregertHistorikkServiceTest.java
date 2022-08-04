package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.ArbeidsmiljøportalenBransje.BARNEHAGER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.SISTE_PUBLISERTE_KVARTAL;
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
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Virksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.Aggregeringstype;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UtilstrekkeligDataException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregertHistorikkServiceTest {

    private final BransjeEllerNæring enBransje = new BransjeEllerNæring(
          new Bransje(BARNEHAGER, "En bransje", "88911"));
    private final Orgnr etOrgnr = new Orgnr("999999999");
    private final Underenhet enBarnehage = Underenhet.builder()
          .orgnr(etOrgnr)
          .navn("En Barnehage")
          .næringskode(new Næringskode5Siffer("88911", "Barnehage"))
          .antallAnsatte(10)
          .overordnetEnhetOrgnr(new Orgnr("1111111111")).build();


    private AggregertStatistikkService serviceUnderTest;
    @Mock
    private SykefraværRepository mockSykefraværRepository;
    @Mock
    private TilgangskontrollService mockTilgangskontrollService;
    @Mock
    private EnhetsregisteretClient mockEnhetsregisteretClient;
    @Mock
    private BransjeEllerNæringService mockBransjeEllerNæringService;

    private static UmaskertSykefraværForEttKvartal umaskertSykefraværprosent(
          ÅrstallOgKvartal årstallOgKvartal, double prosent) {
        return umaskertSykefraværprosent(årstallOgKvartal, prosent, 10);
    }

    private static UmaskertSykefraværForEttKvartal umaskertSykefraværprosent(
          ÅrstallOgKvartal årstallOgKvartal, double prosent, int antallPersoner) {
        return new UmaskertSykefraværForEttKvartal(årstallOgKvartal, new BigDecimal(prosent),
              new BigDecimal(100), antallPersoner
        );
    }

    @BeforeEach
    public void setUp() {

        serviceUnderTest = new AggregertStatistikkService(
              mockSykefraværRepository,
              mockBransjeEllerNæringService,
              mockTilgangskontrollService,
              mockEnhetsregisteretClient
        );
    }

    @AfterEach
    public void tearDown() {
        reset(
              mockSykefraværRepository,
              mockEnhetsregisteretClient,
              mockBransjeEllerNæringService,
              mockTilgangskontrollService);
    }

    @Test
    void hentAggregertHistorikk_kræsjerIkkeVedManglendeData() {
        when(mockTilgangskontrollService.brukerRepresentererVirksomheten(any())).thenReturn(true);
        when(mockEnhetsregisteretClient.hentUnderenhet(any())).thenReturn(enBarnehage);
        when(mockTilgangskontrollService.brukerHarIaRettigheter(any())).thenReturn(true);
        when(mockBransjeEllerNæringService.skalHenteDataPåBransjeEllerNæringsnivå(any()))
              .thenReturn(enBransje);

        // Helt tomt resultat skal ikke kræsje
        when(mockSykefraværRepository.hentUmaskertSykefraværAlleKategorier(any(), any()))
              .thenReturn(new Sykefraværsdata(Map.of()));
        assertThat(serviceUnderTest.hentAggregertStatistikk(etOrgnr).get())
              .isEqualTo(List.of());

        // Resultat med statistikkategri og tomme lister skal heller ikke kræsje
        when(mockSykefraværRepository.hentUmaskertSykefraværAlleKategorier(any(), any())).thenReturn(
              new Sykefraværsdata(Map.of(
                    VIRKSOMHET, List.of(),
                    LAND, List.of(),
                    NÆRING, List.of(),
                    BRANSJE, List.of()
              )));
        assertThat(serviceUnderTest.hentAggregertStatistikk(etOrgnr).get())
              .isEqualTo(List.of());
    }

    @Test
    void hentAggregertHistorikk_henterAltSykefraværDersomBrukerHarIaRettigheter() {
        when(mockTilgangskontrollService.brukerRepresentererVirksomheten(any())).thenReturn(true);
        when(mockEnhetsregisteretClient.hentUnderenhet(any())).thenReturn(enBarnehage);
        when(mockTilgangskontrollService.brukerHarIaRettigheter(any())).thenReturn(true);
        when(mockBransjeEllerNæringService.skalHenteDataPåBransjeEllerNæringsnivå(any()))
              .thenReturn(enBransje);

        when(mockSykefraværRepository.hentUmaskertSykefraværAlleKategorier(any(), any()))
              .thenReturn(new Sykefraværsdata(
                    Map.of(
                          VIRKSOMHET, genererTestSykefravær(1),
                          LAND, genererTestSykefravær(10),
                          NÆRING, genererTestSykefravær(20),
                          BRANSJE, genererTestSykefravær(30)
                    )
              ));

        List<Aggregeringstype> forventedeStatistikktyper =
              List.of(
                    Aggregeringstype.PROSENT_SISTE_4_KVARTALER_VIRKSOMHET,
                    Aggregeringstype.PROSENT_SISTE_4_KVARTALER_BRANSJE,
                    Aggregeringstype.PROSENT_SISTE_4_KVARTALER_LAND,
                    Aggregeringstype.TREND_BRANSJE
              );

        List<Aggregeringstype> statistikktyper =
              serviceUnderTest.hentAggregertStatistikk(etOrgnr).get().stream()
                    .map(AggregertStatistikkDto::getType)
                    .collect(Collectors.toList());
        assertThat(statistikktyper).isEqualTo(forventedeStatistikktyper);
    }

    @Test
    void kalkulerTrend_skal_returnere_ManglendeDataException_ved_mangel_av_ett_kvartal() {
        assertThat(new Trendkalkulator(
              List.of(umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 2), 11, 1),
                    umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 1), 10, 3)
              )).kalkulerTrend().getLeft()).isExactlyInstanceOf(
              UtilstrekkeligDataException.class);
    }

    // TODO: er det 4 eller 20 som er forventet?
    @Test
    void kalkulerTrend_skal_returnere_stigende_ved_økende_trend() {
        ÅrstallOgKvartal k1 = new ÅrstallOgKvartal(2022, 1);
        ÅrstallOgKvartal k2 = new ÅrstallOgKvartal(2021, 1);
        assertThat(new Trendkalkulator(List.of(umaskertSykefraværprosent(k1, 3),
              umaskertSykefraværprosent(k2, 2)
        )).kalkulerTrend().get()).isEqualTo(new Trend(new BigDecimal("1.00"), 20, List.of(k1, k2)));
    }

    @Test
    void kalkulerTrend_skal_returnere_synkende_ved_nedadgående_trend() {
        List<UmaskertSykefraværForEttKvartal> kvartalstall =
              List.of(umaskertSykefraværprosent(SISTE_PUBLISERTE_KVARTAL, 8, 1),
                    umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 2), 13, 2),
                    umaskertSykefraværprosent(SISTE_PUBLISERTE_KVARTAL.minusEttÅr(), 10, 3)
              );
        Trend forventetTrend = new Trend(
              new BigDecimal("-2.00"),
              4,
              List.of(SISTE_PUBLISERTE_KVARTAL, SISTE_PUBLISERTE_KVARTAL.minusEttÅr()));

        assertThat(new Trendkalkulator(kvartalstall).kalkulerTrend().get()).isEqualTo(
              forventetTrend);
    }

    @Test
    void kalkulerTrend_skal_returnere_tåle_tomt_datagrunnlag() {
        assertThat(new Trendkalkulator(List.of()).kalkulerTrend().getLeft()).isExactlyInstanceOf(
              UtilstrekkeligDataException.class);
    }

    private void lagTestDataTilRepository() {
        when(mockSykefraværRepository.hentUmaskertSykefraværAlleKategorier(any(Virksomhet.class),
              any(ÅrstallOgKvartal.class)
        )).thenReturn(new Sykefraværsdata(
              Map.of(VIRKSOMHET, genererTestSykefravær(0),
                    NÆRING, genererTestSykefravær(10),
                    BRANSJE,
                    genererTestSykefravær(20), LAND, genererTestSykefravær(30)
              )));
    }

    private List<UmaskertSykefraværForEttKvartal> genererTestSykefravær(int offset) {
        return Arrays.asList(
              umaskertSykefraværprosent(new ÅrstallOgKvartal(2022, 1), 1.1 + offset),
              umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 4), 2.2 + offset),
              umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 3), 3.3 + offset),
              umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 2), 4.4 + offset),
              umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 1), 5.5 + offset),
              umaskertSykefraværprosent(new ÅrstallOgKvartal(2020, 4), 6.6 + offset),
              umaskertSykefraværprosent(new ÅrstallOgKvartal(2020, 3), 7.7 + offset)
        );
    }

}
