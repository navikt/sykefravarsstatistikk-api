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
import java.util.Optional;

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
    void hentGenerellStatistikk_skal_hente_virksomhet_prosent_når_virksomhet_har_ikke_maskert_statistikk() {
        lagTestDataTilRepository();
        Underenhet underenhet = new Underenhet(
                new Orgnr("987654321"),
                new Orgnr("999888777"),
                "Test underenhet 2",
                new Næringskode5Siffer("88911", "Barnehager"),
                15
        );
        assertThat(oppsummertSykefravarsstatistikkService.hentOgBearbeidStatistikk(underenhet)).isEqualTo(
                Optional.of(new GenerellStatistikk(
                        Statistikkategori.VIRKSOMHET,
                        "Test underenhet 2",
                        "10.5"
                ))
        );

    }

    @Test
    void hentUmaskertStatistikkForSisteFemKvartaler_skal_hente_maks_5_sista_kvartaler() {
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
        when(sykefraværRepository.hentUmaskertSykefraværForEttKvartalListe(any(Virksomhet.class), any(ÅrstallOgKvartal.class)))
                .thenReturn(
                        Arrays.asList(
                                umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 2), 11),
                                umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 1), 10)
                        )
                );
        when(sykefraværRepository.getAllTheThings(any(Virksomhet.class), any(ÅrstallOgKvartal.class)))
                .thenReturn(Map.of(Statistikkategori.VIRKSOMHET,
                        Arrays.asList(// TODO fiks dette og gjern de gamle kvartaler etter test som sikrer riktig data from repository er på plass
                                umaskertSykefraværprosent(new ÅrstallOgKvartal(2022, 1), 11),
                                umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 4), 10),
                                umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 3), 10),
                                umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 2), 10),
                                umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 1), 10),
                                umaskertSykefraværprosent(new ÅrstallOgKvartal(2020, 4), 10),
                                umaskertSykefraværprosent(new ÅrstallOgKvartal(2020, 3), 10)
                        )
                ));
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
