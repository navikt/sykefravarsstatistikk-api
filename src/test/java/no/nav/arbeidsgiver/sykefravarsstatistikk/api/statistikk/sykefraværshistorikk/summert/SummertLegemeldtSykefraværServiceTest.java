package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.LegemeldtSykefraværsprosent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SummertLegemeldtSykefraværServiceTest {

    @Mock
    private SykefraværRepository sykefraværRepository;

    @InjectMocks
    private SummertLegemeldtSykefraværService summertLegemeldtSykefraværService;


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
                new ÅrstallOgKvartal(2021, 2)
        );

        assertThat(legemeldtSykefraværsprosent).isNotNull();
        assertThat(legemeldtSykefraværsprosent.getLabel()).isEqualTo("Test underenhet 2");
        assertThat(legemeldtSykefraværsprosent.getProsent()).isEqualTo(new BigDecimal(10.5));
    }


    private void lagTestDataTilRepository() {
        when(sykefraværRepository.hentUmaskertSykefraværForEttKvartalListe(any(), any()))
                .thenReturn(
                        Arrays.asList(
                                umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 2), 11),
                                umaskertSykefraværprosent(new ÅrstallOgKvartal(2021, 1), 10)
                                )
                );
    }

    private static UmaskertSykefraværForEttKvartal umaskertSykefraværprosent(
            ÅrstallOgKvartal årstallOgKvartal,
            double prosent
    ) {
        return new UmaskertSykefraværForEttKvartal(
                årstallOgKvartal,
                new BigDecimal(prosent),
                new BigDecimal(100),
                10
        );
    }
}
