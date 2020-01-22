package no.nav.tag.sykefravarsstatistikk.api.domene.statistikk;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ÅrstallOgKvartalTest {

    @Test
    public void minusKvartaler__skal_retunere_rikitg() {
        assertThat(new ÅrstallOgKvartal(2019,3).minusKvartaler(1)).isEqualTo(new ÅrstallOgKvartal(2019,2));
        assertThat(new ÅrstallOgKvartal(2019,4).minusKvartaler(1)).isEqualTo(new ÅrstallOgKvartal(2019,3));
        assertThat(new ÅrstallOgKvartal(2019,1).minusKvartaler(1)).isEqualTo(new ÅrstallOgKvartal(2018,4));
        assertThat(new ÅrstallOgKvartal(2019,3).minusKvartaler(3)).isEqualTo(new ÅrstallOgKvartal(2018,4));
        assertThat(new ÅrstallOgKvartal(2019,3).minusKvartaler(11)).isEqualTo(new ÅrstallOgKvartal(2016,4));

    }
}
