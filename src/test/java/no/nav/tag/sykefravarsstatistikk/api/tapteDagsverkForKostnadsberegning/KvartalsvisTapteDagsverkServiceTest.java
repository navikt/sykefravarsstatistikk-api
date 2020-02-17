package no.nav.tag.sykefravarsstatistikk.api.tapteDagsverkForKostnadsberegning;

import no.nav.tag.sykefravarsstatistikk.api.domene.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static no.nav.tag.sykefravarsstatistikk.api.TestData.etOrgnr;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class KvartalsvisTapteDagsverkServiceTest {
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
        Orgnr orgnr = etOrgnr();
        tapteDagsverkService.hentTapteDagsverkFraDeSiste4Kvartalene(orgnr);

        verify(repository, times(1)).hentTapteDagsverkFor4Kvartaler(any(), eq(orgnr));
    }

    @Test
    public void oppsummerTapteDagsverk__skal_sende_riktige_årstall_og_kvartaler() {
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
        tapteDagsverkService.hentOppsummertTapteDagsverk(null);

        verify(repository, times(1)).hentTapteDagsverkFor4Kvartaler(eq(årstallOgKvartal), any());
        //tapteDagsverkService.oppsummerTapteDagsverk()
        //TapteDagsverk
        //tapteDagsverkService.hentTapteDagsverkFraDeSiste4Kvartalene(null);

        //verify(repository, times(1)).hentTapteDagsverkFor4Kvartaler(eq(årstallOgKvartal), any());
    }

    @Test
    public void oppsummerTapteDagsverk__skal_sende_rikitg_underenhet() {
        Orgnr orgnr = etOrgnr();
        tapteDagsverkService.hentOppsummertTapteDagsverk(orgnr);

              verify(repository, times(1)).hentTapteDagsverkFor4Kvartaler(any(), eq(orgnr));
    }
}
