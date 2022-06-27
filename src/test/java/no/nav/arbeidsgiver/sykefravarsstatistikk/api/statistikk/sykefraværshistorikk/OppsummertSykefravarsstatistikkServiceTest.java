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
                        tilgangskontrollService,
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

    private void lagTestDataTilRepository() {
        when(sykefraværRepository.hentUmaskertSykefraværForEttKvartalListe(any(Virksomhet.class), any(Kvartal.class)))
                .thenReturn(
                        Arrays.asList(
                                umaskertSykefraværprosent(new Kvartal(2021, 2), 11),
                                umaskertSykefraværprosent(new Kvartal(2021, 1), 10)
                        )
                );
    }

    private static UmaskertSykefraværForEttKvartal umaskertSykefraværprosent(
            Kvartal kvartal,
            double prosent
    ) {
        return umaskertSykefraværprosent(kvartal, prosent, 10);
    }

    private static UmaskertSykefraværForEttKvartal umaskertSykefraværprosent(
            Kvartal kvartal,
            double prosent,
            int antallPersoner
    ) {
        return new UmaskertSykefraværForEttKvartal(
                kvartal,
                new BigDecimal(prosent),
                new BigDecimal(100),
                antallPersoner
        );
    }

}
