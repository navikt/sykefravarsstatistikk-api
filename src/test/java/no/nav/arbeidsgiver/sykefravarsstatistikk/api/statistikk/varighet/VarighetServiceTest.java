package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.varighet;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.SummertSykefraværshistorikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.VarighetRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert.VarighetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VarighetServiceTest {

    @Mock
    private VarighetRepository varighetRepository;
    @Mock
    private KlassifikasjonerRepository klassifikasjonerRepository;

    private VarighetService varighetService;

    private Underenhet barnehage;

    @BeforeEach
    public void setUp() {
        varighetService = new VarighetService(varighetRepository, new Bransjeprogram(), klassifikasjonerRepository);
        barnehage = Underenhet.builder().orgnr(new Orgnr("999999999"))
                .navn("test Barnehage")
                .næringskode(new Næringskode5Siffer("88911", "Barnehage"))
                .antallAnsatte(10)
                .overordnetEnhetOrgnr(new Orgnr("1111111111")).build();
    }


    @Test
    public void hentSummertKorttidsOgLangtidsfraværForBransjeEllerNæring__() {
        new UmaskertSykefraværForEttKvartalMedVarighet(
                new ÅrstallOgKvartal(2020, 1),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(0),
                0,
                Varighetskategori._1_DAG_TIL_7_DAGER
        );
        List<UmaskertSykefraværForEttKvartalMedVarighet> sykefraværMed1Kvartal = Arrays.asList(
                new UmaskertSykefraværForEttKvartalMedVarighet(
                        new ÅrstallOgKvartal(2020, 1),
                        BigDecimal.valueOf(5),
                        BigDecimal.valueOf(0),
                        0,
                        Varighetskategori._1_DAG_TIL_7_DAGER
                ),
                new UmaskertSykefraværForEttKvartalMedVarighet(
                        new ÅrstallOgKvartal(2020, 1),
                        BigDecimal.valueOf(0),
                        BigDecimal.valueOf(10),
                        2,
                        Varighetskategori.TOTAL
                )
        );
        when(varighetRepository.hentSykefraværForEttKvartalMedVarighet(any(Bransje.class))).thenReturn(sykefraværMed1Kvartal);

        SummertSykefraværshistorikk summertSykefraværshistorikk =
                varighetService.hentSummertKorttidsOgLangtidsfraværForBransjeEllerNæring(
                        barnehage,
                        new ÅrstallOgKvartal(2020, 1),
                        4
                );

        assertThat(summertSykefraværshistorikk.getType()).isEqualTo(Statistikkategori.BRANSJE);
        assertThat(summertSykefraværshistorikk.getLabel()).isEqualTo("Barnehager");
        assertThat(summertSykefraværshistorikk.getSummertKorttidsOgLangtidsfravær().getSummertKorttidsfravær()).isNotNull();
        assertThat(summertSykefraværshistorikk.getSummertKorttidsOgLangtidsfravær().getSummertLangtidsfravær()).isNotNull();
    }

}
