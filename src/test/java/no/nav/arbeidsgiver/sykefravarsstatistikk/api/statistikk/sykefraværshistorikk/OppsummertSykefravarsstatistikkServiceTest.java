package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SykefraværRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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

    @BeforeEach
    public void setUp() {
        oppsummertSykefravarsstatistikkService =
              new OppsummertSykefravarsstatistikkService(
                    sykefraværRepository,
                    new BransjeEllerNæringService(
                          new Bransjeprogram(),
                          klassifikasjonerRepository
                    ),
                    enhetsregisteretClient
              );
    }

    @Test
    void hentOgBearbeidStatistikk_kræsjerIkkeVedManglendeData() {
        when(sykefraværRepository.hentUmaskertSykefraværAlleKategorier(any(), any())).thenReturn(Map.of());
        Underenhet virksomhetUtenData = new Underenhet(
              new Orgnr("987654321"),
              new Orgnr("999888777"),
              "Testvirksomhet",
              new Næringskode5Siffer("88911", "Barnehager"),
              15
        );

        List<GenerellStatistikk> tomListe = List.of();
        assertThat(oppsummertSykefravarsstatistikkService.hentOgBearbeidStatistikk(virksomhetUtenData))
              .isEqualTo(tomListe);
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

        List<GenerellStatistikk> forventetData = List.of(

        );

        assertThat(oppsummertSykefravarsstatistikkService.hentOgBearbeidStatistikk(virksomhet)).isEqualTo(forventetData);

    }

    @Test
    void hentUmaskertStatistikkForSisteFemKvartaler_skal_hente_maks_5_siste_kvartaler() {
        lagTestDataTilRepository();
        Underenhet underenhet = new Underenhet(
              new Orgnr("987654321"),
              new Orgnr("999888777"),
              "Test underenhet 2",
              new Næringskode5Siffer("88911", "Barnehager"),
              15
        );
        assertThat(
              oppsummertSykefravarsstatistikkService.hentUmaskertStatistikkForSisteFemKvartaler(
                    underenhet
              )).isEqualTo(
              Map.of(Statistikkategori.VIRKSOMHET,
                    Arrays.asList(
                          umaskertSykefraværprosent(new ÅrstallOgKvartal(2022, 1), 11),
                          umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 4), 10),
                          umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 3), 10),
                          umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 2), 10),
                          umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 1), 10)
                    )
              )
        );
    }

    @Test
    void kalkulerTrend_skal_returnere_ufullstendigdata_ved_mangel_av_ett_kvartal() {
        assertThat(oppsummertSykefravarsstatistikkService.kalkulerTrend(Arrays.asList(
                    umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 2), 11),
                    umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 1), 10)
              )
        )).isEqualTo("UfullstendigData");
    }

    @Test
    void kalkulerTrend_skal_returnere_stigende_ved_økende_trend() {
        assertThat(oppsummertSykefravarsstatistikkService.kalkulerTrend(Arrays.asList(
                    umaskertSykefraværprosent(new ÅrstallOgKvartal(2022, 1), 11),
                    umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 1), 10)
              )
        )).isEqualTo(
              "1.00"
        );
    }

    @Test
    void kalkulerTrend_skal_returnere_synkende_ved_nedadgående_trend() {
        assertThat(oppsummertSykefravarsstatistikkService.kalkulerTrend(Arrays.asList(
              umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 1), 10),
              umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 2), 13),
              umaskertSykefraværprosent(new ÅrstallOgKvartal(2022, 1), 9)
        ))).isEqualTo("-1.00");
    }

    @Test
    void kalkulerTrend_skal_returnere_tåle_tomt_datagrunnlag() {
        assertThat(oppsummertSykefravarsstatistikkService.kalkulerTrend(List.of()))
              .isEqualTo("UfullstendigData");
    }

    private void lagTestDataTilRepository() {
        when(sykefraværRepository.hentUmaskertSykefraværAlleKategorier(any(Virksomhet.class), any(ÅrstallOgKvartal.class)))
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

}
