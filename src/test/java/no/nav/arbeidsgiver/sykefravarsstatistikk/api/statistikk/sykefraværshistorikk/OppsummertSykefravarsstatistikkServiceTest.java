package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.OppsummertSykefravarsstatistikkService.Trend;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OppsummertSykefravarsstatistikkServiceTest {

    @Mock
    private SykefraværRepository sykefraværRepository;

    @Mock
    private KlassifikasjonerRepository klassifikasjonerRepository;
    @Mock
    private TilgangskontrollService tilgangskontrollService;
    @Mock
    private EnhetsregisteretClient enhetsregisteretClient;

    private OppsummertSykefravarsstatistikkService oppsummertSykefravarsstatistikkService;

    private static UmaskertSykefraværForEttKvartal umaskertSykefraværprosent(
          ÅrstallOgKvartal årstallOgKvartal,
          double prosent
    ) {
        return umaskertSykefraværprosent(årstallOgKvartal, prosent, 10);
    }

    private static UmaskertSykefraværForEttKvartal umaskertSykefraværprosent(
          ÅrstallOgKvartal årstallOgKvartal,
          double prosent,
          int antallPersoner
    ) {
        return new UmaskertSykefraværForEttKvartal(
              årstallOgKvartal,
              new BigDecimal(prosent),
              new BigDecimal(100),
              antallPersoner
        );
    }

    @BeforeEach
    public void setUp() {
        oppsummertSykefravarsstatistikkService =
              new OppsummertSykefravarsstatistikkService(
                    sykefraværRepository,
                    new BransjeEllerNæringService(
                          new Bransjeprogram(),
                          klassifikasjonerRepository
                    ),
                    tilgangskontrollService,
                    enhetsregisteretClient
              );
    }

    @Test
    void hentOgBearbeidStatistikk_kræsjerIkkeVedManglendeData() {
        when(sykefraværRepository.hentUmaskertSykefraværAlleKategorier(any(), any())).thenReturn(
              Map.of());
        Underenhet virksomhetUtenData = new Underenhet(
              new Orgnr("987654321"),
              new Orgnr("999888777"),
              "Testvirksomhet",
              new Næringskode5Siffer("88911", "Barnehager"),
              15
        );

        assertThat(
              oppsummertSykefravarsstatistikkService.hentOgBearbeidStatistikk(virksomhetUtenData))
              .isEqualTo(List.of());
    }

    @Test
    void hentOgBearbeidStatistikk_henterSykefraværFraAlleKategorier() {
        Underenhet virksomhet = new Underenhet(
              new Orgnr("987654321"),
              new Orgnr("999888777"),
              "Testvirksomhet",
              new Næringskode5Siffer("88911", "Barnehager"),
              15
        );
        lagTestDataTilRepository();
        List<Statistikkategori> forventedeStatistikktyper = List.of(
              Statistikkategori.VIRKSOMHET, Statistikkategori.BRANSJE, Statistikkategori.LAND,
              Statistikkategori.TREND_BRANSJE);

        List<Statistikkategori> statistikktyper =
              oppsummertSykefravarsstatistikkService.hentOgBearbeidStatistikk(virksomhet).stream()
                    .map(OppsummertStatistikkDto::getType)
                    .collect(Collectors.toList());
        assertThat(statistikktyper).isEqualTo(forventedeStatistikktyper);
    }

    @Test
    void kalkulerTrend_skal_returnere_ufullstendigdata_ved_mangel_av_ett_kvartal() {
        assertThat(Trend.kalkulerTrend(Arrays.asList(
                    umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 2), 11, 1),
                    umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 1), 10, 3)
              )
        )).isEqualTo(Trend.NULLPUNKT);
    }

    @Test
    void kalkulerTrend_skal_returnere_stigende_ved_økende_trend() {
        ÅrstallOgKvartal k1 = new ÅrstallOgKvartal(2022, 1);
        ÅrstallOgKvartal k2 = new ÅrstallOgKvartal(2021, 1);
        assertThat(Trend.kalkulerTrend(Arrays.asList(
                    umaskertSykefraværprosent(k1, 3),
                    umaskertSykefraværprosent(k2, 2)
              )
        )).isEqualTo(
              new Trend(new BigDecimal("1.00"), 4, List.of(k1, k2))
        );
    }

    @Test
    void kalkulerTrend_skal_returnere_synkende_ved_nedadgående_trend() {
        List<UmaskertSykefraværForEttKvartal> kvartaler = List.of(
              umaskertSykefraværprosent(
                    new ÅrstallOgKvartal(2021, 1), 10, 1),
              umaskertSykefraværprosent(
                    new ÅrstallOgKvartal(2021, 2), 13, 2),
              umaskertSykefraværprosent(
                    new ÅrstallOgKvartal(2022, 1), 9, 3)
        );
        Trend forventetTrend = new Trend(new BigDecimal("-1.00"), 5,
              kvartaler.stream()
                    .map(UmaskertSykefraværForEttKvartal::getÅrstallOgKvartal)
                    .collect(Collectors.toList()));

        assertThat(Trend.kalkulerTrend(kvartaler)).isEqualTo(forventetTrend);
    }

    @Test
    void kalkulerTrend_skal_returnere_tåle_tomt_datagrunnlag() {
        assertThat(Trend.kalkulerTrend(List.of()))
              .isEqualTo(Trend.NULLPUNKT);
    }

    private void lagTestDataTilRepository() {
        when(sykefraværRepository.hentUmaskertSykefraværAlleKategorier(any(Virksomhet.class),
              any(ÅrstallOgKvartal.class)))
              .thenReturn(
                    Map.of(
                          Statistikkategori.VIRKSOMHET,
                          genererTestSykefravær(0),
                          Statistikkategori.NÆRING,
                          genererTestSykefravær(10),
                          Statistikkategori.BRANSJE,
                          genererTestSykefravær(20),
                          Statistikkategori.LAND,
                          genererTestSykefravær(30)
                    ));
    }

    private List<UmaskertSykefraværForEttKvartal> genererTestSykefravær(int offset) {
        return Arrays.asList(
              umaskertSykefraværprosent(new ÅrstallOgKvartal(2022, 1), 1 + offset),
              umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 4), 23 + offset),
              umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 3), 13 + offset),
              umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 2), 45 + offset),
              umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 1), 34 + offset),
              umaskertSykefraværprosent(new ÅrstallOgKvartal(2020, 4), 23 + offset),
              umaskertSykefraværprosent(new ÅrstallOgKvartal(2020, 3), 2 + offset)
        );
    }

}
