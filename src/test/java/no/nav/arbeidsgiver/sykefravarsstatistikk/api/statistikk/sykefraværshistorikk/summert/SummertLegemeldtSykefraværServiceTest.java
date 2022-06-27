package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæringService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.LegemeldtSykefraværsprosent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SummertLegemeldtSykefraværServiceTest {

    @Mock
    private SykefraværRepository sykefraværRepository;

    @Mock
    private KlassifikasjonerRepository klassifikasjonerRepository;

    private SummertLegemeldtSykefraværService summertLegemeldtSykefraværService;

    @BeforeEach
    public void setUp() {
        summertLegemeldtSykefraværService = new SummertLegemeldtSykefraværService(
                sykefraværRepository,
                new BransjeEllerNæringService(
                        new Bransjeprogram(),
                        klassifikasjonerRepository
                )
        );
    }

    @Test
    public void legemeldtSykefraværsprosent_utleddes_fra_siste_4_kvartaler() {
        lagTestDataTilRepository();
        Underenhet underenhet = new Underenhet(
                new Orgnr("987654321"),
                new Orgnr("999888777"),
                "Test underenhet 2",
                new Næringskode5Siffer("88911", "Barnehager"),
                15
        );

        LegemeldtSykefraværsprosent legemeldtSykefraværsprosent = summertLegemeldtSykefraværService.hentLegemeldtSykefraværsprosent(
                underenhet,
                new Kvartal(2021, 2)
        );

        assertThat(legemeldtSykefraværsprosent).isNotNull();
        assertThat(legemeldtSykefraværsprosent.getType()).isEqualTo(Statistikkategori.VIRKSOMHET);
        assertThat(legemeldtSykefraværsprosent.getLabel()).isEqualTo("Test underenhet 2");
        assertThat(legemeldtSykefraværsprosent.getProsent()).isEqualTo(new BigDecimal(10.5));
    }


    @Test
    public void legemeldtSykefraværsprosent_henter_bransje_sykefraværssprosent_dersom_prosent_er_masker_for_bedriften() {
        lagTestDataTilRepository(4);
        Underenhet underenhet = new Underenhet(
                new Orgnr("987654321"),
                new Orgnr("999888777"),
                "Test underenhet 2",
                new Næringskode5Siffer("88911", "Barnehager"),
                15
        );

        LegemeldtSykefraværsprosent legemeldtSykefraværsprosent = summertLegemeldtSykefraværService.hentLegemeldtSykefraværsprosent(
                underenhet,
                new Kvartal(2021, 2)
        );

        assertThat(legemeldtSykefraværsprosent).isNotNull();
        assertThat(legemeldtSykefraværsprosent.getType()).isEqualTo(Statistikkategori.BRANSJE);
        assertThat(legemeldtSykefraværsprosent.getLabel()).isEqualTo("Barnehager");
        assertThat(legemeldtSykefraværsprosent.getProsent()).isEqualTo(new BigDecimal(10.5));
    }

    @Test
    public void legemeldtSykefraværsprosent_for_virksomheter_som_ikke_har_data_skal_returnere_bransje__dersom_virksomhet_er_i_bransjeprogram() {
        lagTestDataTilRepositoryForBransje();
        LegemeldtSykefraværsprosent legemeldtSykefraværsprosent =
                summertLegemeldtSykefraværService.hentLegemeldtSykefraværsprosent(
                        new Underenhet(
                                new Orgnr("456456456"),
                                new Orgnr("654654654"),
                                "Underenhet uten data i DB",
                                new Næringskode5Siffer("88911", "Barnehager"),
                                0
                        ),
                        new Kvartal(2021, 2)
                );

        assertThat(legemeldtSykefraværsprosent).isNotNull();
        assertThat(legemeldtSykefraværsprosent.getType()).isEqualTo(Statistikkategori.BRANSJE);
        assertThat(legemeldtSykefraværsprosent.getLabel()).isEqualTo("Barnehager");
        assertThat(legemeldtSykefraværsprosent.getProsent()).isEqualTo(new BigDecimal(8.5));
    }

    @Test
    public void legemeldtSykefraværsprosent_for_virksomheter_som_ikke_har_data_skal_returnere_næring__dersom_virksomhet_ikke_er_i_bransjeprogram() {
        lagTestDataTilRepositoryForNæring();
        LegemeldtSykefraværsprosent legemeldtSykefraværsprosent =
                summertLegemeldtSykefraværService.hentLegemeldtSykefraværsprosent(
                        new Underenhet(
                                new Orgnr("456456456"),
                                new Orgnr("654654654"),
                                "Underenhet uten data i DB",
                                new Næringskode5Siffer("88913", "Skolefritidsordninger"),
                                0
                        ),
                        new Kvartal(2021, 2)
                );

        assertThat(legemeldtSykefraværsprosent).isNotNull();
        assertThat(legemeldtSykefraværsprosent.getType()).isEqualTo(Statistikkategori.NÆRING);
        assertThat(legemeldtSykefraværsprosent.getLabel()).isEqualTo("Skolefritidsordninger");
        assertThat(legemeldtSykefraværsprosent.getProsent()).isEqualTo(new BigDecimal(5.5));
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

    private void lagTestDataTilRepository(int antallPersoner) {
        when(sykefraværRepository.hentUmaskertSykefraværForEttKvartalListe(any(Virksomhet.class), any(Kvartal.class)))
                .thenReturn(
                        Arrays.asList(
                                umaskertSykefraværprosent(new Kvartal(2021, 2), 11, antallPersoner),
                                umaskertSykefraværprosent(new Kvartal(2021, 1), 10, antallPersoner)
                        )
                );

        when(sykefraværRepository.hentUmaskertSykefraværForEttKvartalListe(any(Bransje.class), any(Kvartal.class)))
                .thenReturn(
                        Arrays.asList(
                                umaskertSykefraværprosent(new Kvartal(2021, 2), 11),
                                umaskertSykefraværprosent(new Kvartal(2021, 1), 10)
                        )
                );
    }

    private void lagTestDataTilRepositoryForBransje() {
        when(sykefraværRepository.hentUmaskertSykefraværForEttKvartalListe(any(Virksomhet.class), any(Kvartal.class)))
                .thenReturn(Collections.emptyList());

        when(sykefraværRepository.hentUmaskertSykefraværForEttKvartalListe(any(Bransje.class), any(Kvartal.class)))
                .thenReturn(
                        Arrays.asList(
                                umaskertSykefraværprosent(new Kvartal(2021, 2), 8),
                                umaskertSykefraværprosent(new Kvartal(2021, 1), 9)
                        )
                );
    }

    private void lagTestDataTilRepositoryForNæring() {
        when(sykefraværRepository.hentUmaskertSykefraværForEttKvartalListe(any(Virksomhet.class), any(Kvartal.class)))
                .thenReturn(Collections.emptyList());

        when(sykefraværRepository.hentUmaskertSykefraværForEttKvartalListe(any(Næring.class), any(Kvartal.class)))
                .thenReturn(
                        Arrays.asList(
                                umaskertSykefraværprosent(new Kvartal(2021, 2), 5),
                                umaskertSykefraværprosent(new Kvartal(2021, 1), 6)
                        )
                );
        when(klassifikasjonerRepository.hentNæring(any())).thenReturn(
                new Næring("88913", "Skolefritidsordninger")
        );
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

    private static UmaskertSykefraværForEttKvartal umaskertSykefraværprosent(
            Kvartal kvartal,
            double prosent
    ) {
        return umaskertSykefraværprosent(kvartal, prosent, 10);
    }
}
