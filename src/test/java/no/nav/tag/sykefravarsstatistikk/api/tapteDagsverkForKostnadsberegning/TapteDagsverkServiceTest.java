package no.nav.tag.sykefravarsstatistikk.api.tapteDagsverkForKostnadsberegning;

import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.tag.sykefravarsstatistikk.api.enhetsregisteret.Underenhet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static no.nav.tag.sykefravarsstatistikk.api.TestData.enUnderenhet;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        Underenhet underenhet = enUnderenhet();
        tapteDagsverkService.hentTapteDagsverkFraDeSiste4Kvartalene(underenhet);

        verify(repository, times(1)).hentTapteDagsverkFor4Kvartaler(any(), eq(underenhet));
    }
}
