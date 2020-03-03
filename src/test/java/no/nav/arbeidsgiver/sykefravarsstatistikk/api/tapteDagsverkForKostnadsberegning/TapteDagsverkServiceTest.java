package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tapteDagsverkForKostnadsberegning;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TapteDagsverkServiceTest {
    @Mock
    TapteDagsverkForKostnadsberegningRepository repository;
    private TapteDagsverkService tapteDagsverkService;

    @Before
    public void setup() {
        tapteDagsverkService = new TapteDagsverkService(2019, 3, repository);
    }

    @Test
    public void hentTapteDagsverkFraDeSiste4Kvartalene__skal_sende_riktige_årstall_og_kvartaler() {
        List<ÅrstallOgKvartal> årstallOgKvartal = Arrays.asList(
                new ÅrstallOgKvartal(2019, 3),
                new ÅrstallOgKvartal(2019, 2),
                new ÅrstallOgKvartal(2019, 1),
                new ÅrstallOgKvartal(2018, 4)
        );

        tapteDagsverkService.hentTapteDagsverkFraDeSiste4Kvartalene(null);

        verify(repository, times(1)).hentTapteDagsverkFor4Kvartaler(eq(årstallOgKvartal), any());
    }

    @Test
    public void hentTapteDagsverkFraDeSiste4Kvartalene__skal_sende_rikitg_underenhet() {
        Orgnr orgnr = TestData.etOrgnr();
        tapteDagsverkService.hentTapteDagsverkFraDeSiste4Kvartalene(orgnr);

        verify(repository, times(1)).hentTapteDagsverkFor4Kvartaler(any(), eq(orgnr));
    }

    @Test
    public void summerTapteDagsverk__skal_sende_riktige_årstall_og_kvartaler() {
        List<KvartalsvisTapteDagsverk> kvartalsvisTapteDagsverksListe = Arrays.asList(
                new KvartalsvisTapteDagsverk(new BigDecimal(100), 2019, 3, 50),
                new KvartalsvisTapteDagsverk(new BigDecimal(100), 2019, 2, 50),
                new KvartalsvisTapteDagsverk(new BigDecimal(100), 2019, 1, 50),
                new KvartalsvisTapteDagsverk(new BigDecimal(100), 2018, 4, 50)
        );
        List<ÅrstallOgKvartal> årstallOgKvartal = Arrays.asList(
                new ÅrstallOgKvartal(2019, 3),
                new ÅrstallOgKvartal(2019, 2),
                new ÅrstallOgKvartal(2019, 1),
                new ÅrstallOgKvartal(2018, 4)
        );
        tapteDagsverkService.hentOgSummerTapteDagsverk(null);
        verify(repository, times(1)).hentTapteDagsverkFor4Kvartaler(eq(årstallOgKvartal), any());
    }

    @Test
    public void summerTapteDagsverk__skal_sende_rikitg_underenhet() {
        Orgnr orgnr = TestData.etOrgnr();
        tapteDagsverkService.hentOgSummerTapteDagsverk(orgnr);
        verify(repository, times(1)).hentTapteDagsverkFor4Kvartaler(any(), eq(orgnr));
    }

    @Test
    public void summerTapteDagsverk__skal_ikke_maskere_når_antall_personer_er_5_eller_mer() {
        List<KvartalsvisTapteDagsverk> kvartalsvisTapteDagsverkListe = Arrays.asList(
                TestData.testTapteDagsverk(1, 2019, 1, 5),
                TestData.testTapteDagsverk(10, 2019, 2, 6),
                TestData.testTapteDagsverk(100, 2019, 3, 10),
                TestData.testTapteDagsverk(1000, 2018, 4, 100)
        );
        when(repository.hentTapteDagsverkFor4Kvartaler(anyList(), ArgumentMatchers.eq(TestData.etOrgnr()))).thenReturn(kvartalsvisTapteDagsverkListe);
        assertThat(tapteDagsverkService.hentOgSummerTapteDagsverk(TestData.etOrgnr())).isEqualTo(new TapteDagsverk(new BigDecimal(1111).setScale(6), false));
    }

    @Test
    public void summerTapteDagsverk__skal_maskere_når_antall__personer_er_færre_enn_5() {
        List<KvartalsvisTapteDagsverk> kvartalsvisTapteDagsverkListe = Arrays.asList(
                TestData.testTapteDagsverk(1, 2019, 1, 10),
                TestData.testTapteDagsverk(10, 2019, 2, 4),
                TestData.testTapteDagsverk(100, 2019, 3, 10),
                TestData.testTapteDagsverk(1000, 2018, 4, 10)
        );
        when(repository.hentTapteDagsverkFor4Kvartaler(anyList(), ArgumentMatchers.eq(TestData.etOrgnr()))).thenReturn(kvartalsvisTapteDagsverkListe);
        assertThat(tapteDagsverkService.hentOgSummerTapteDagsverk(TestData.etOrgnr())).isEqualTo(new TapteDagsverk(new BigDecimal(0), true));
    }

    @Test
    public void summerTapteDagsverk__skal_maskere_når_repository_retunerer_3_rader() {
        List<KvartalsvisTapteDagsverk> kvartalsvisTapteDagsverkListe = Arrays.asList(
                TestData.testTapteDagsverk(1, 2019, 1, 10),
                TestData.testTapteDagsverk(100, 2019, 3, 10),
                TestData.testTapteDagsverk(1000, 2018, 4, 10)
        );
        when(repository.hentTapteDagsverkFor4Kvartaler(anyList(), ArgumentMatchers.eq(TestData.etOrgnr()))).thenReturn(kvartalsvisTapteDagsverkListe);
        assertThat(tapteDagsverkService.hentOgSummerTapteDagsverk(TestData.etOrgnr())).isEqualTo(new TapteDagsverk(new BigDecimal(0), true));
    }

    @Test
    public void summerTapteDagsverk__skal_maskere_når_repository_retunerer_5_rader() {
        List<KvartalsvisTapteDagsverk> kvartalsvisTapteDagsverkListe = Arrays.asList(
                TestData.testTapteDagsverk(1, 2019, 1, 10),
                TestData.testTapteDagsverk(1, 2019, 2, 10),
                TestData.testTapteDagsverk(1, 2018, 4, 10),
                TestData.testTapteDagsverk(100, 2019, 3, 10),
                TestData.testTapteDagsverk(1000, 2018, 4, 10)
        );
        when(repository.hentTapteDagsverkFor4Kvartaler(anyList(), ArgumentMatchers.eq(TestData.etOrgnr()))).thenReturn(kvartalsvisTapteDagsverkListe);
        assertThat(tapteDagsverkService.hentOgSummerTapteDagsverk(TestData.etOrgnr())).isEqualTo(new TapteDagsverk(new BigDecimal(0), true));
    }

    @Test
    public void summerTapteDagsverk__skal_maskere_når_repository_retunerer_1_rad() {
        List<KvartalsvisTapteDagsverk> kvartalsvisTapteDagsverkListe = Arrays.asList(
                TestData.testTapteDagsverk(10, 2019, 2, 4)
        );
        when(repository.hentTapteDagsverkFor4Kvartaler(anyList(), ArgumentMatchers.eq(TestData.etOrgnr()))).thenReturn(kvartalsvisTapteDagsverkListe);
        assertThat(tapteDagsverkService.hentOgSummerTapteDagsverk(TestData.etOrgnr())).isEqualTo(new TapteDagsverk(new BigDecimal(0), true));
    }

    @Test
    public void summerTapteDagsverk__skal_maskere_når_repository_retunerer_tom_list_0_rad() {
        List<KvartalsvisTapteDagsverk> kvartalsvisTapteDagsverkListe = Arrays.asList(
        );
        when(repository.hentTapteDagsverkFor4Kvartaler(anyList(), ArgumentMatchers.eq(TestData.etOrgnr()))).thenReturn(kvartalsvisTapteDagsverkListe);
        assertThat(tapteDagsverkService.hentOgSummerTapteDagsverk(TestData.etOrgnr())).isEqualTo(new TapteDagsverk(new BigDecimal(0), true));
    }


}

