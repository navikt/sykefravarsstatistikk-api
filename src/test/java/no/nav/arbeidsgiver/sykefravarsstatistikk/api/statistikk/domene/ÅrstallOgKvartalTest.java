package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Kvartal;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class ÅrstallOgKvartalTest {

    @Test
    public void minusKvartaler__skal_retunere_rikitg() {
        assertThat(new Kvartal(2019, 3).minusKvartaler(1)).isEqualTo(new Kvartal(2019, 2));
        assertThat(new Kvartal(2019, 3).minusKvartaler(0)).isEqualTo(new Kvartal(2019, 3));
        assertThat(new Kvartal(2019, 4).minusKvartaler(1)).isEqualTo(new Kvartal(2019, 3));
        assertThat(new Kvartal(2019, 1).minusKvartaler(1)).isEqualTo(new Kvartal(2018, 4));
        assertThat(new Kvartal(2019, 3).minusKvartaler(3)).isEqualTo(new Kvartal(2018, 4));
        assertThat(new Kvartal(2019, 3).minusKvartaler(11)).isEqualTo(new Kvartal(2016, 4));
        assertThat(new Kvartal(2019, 3).minusKvartaler(-1)).isEqualTo(new Kvartal(2019, 4));
    }

    @Test
    public void plussKvartaler__skal_retunere_rikitg() {
        assertThat(new Kvartal(2019, 3).plussKvartaler(1)).isEqualTo(new Kvartal(2019, 4));
        assertThat(new Kvartal(2019, 3).plussKvartaler(0)).isEqualTo(new Kvartal(2019, 3));
        assertThat(new Kvartal(2019, 4).plussKvartaler(1)).isEqualTo(new Kvartal(2020, 1));
        assertThat(new Kvartal(2019, 1).plussKvartaler(1)).isEqualTo(new Kvartal(2019, 2));
        assertThat(new Kvartal(2019, 3).plussKvartaler(3)).isEqualTo(new Kvartal(2020, 2));
        assertThat(new Kvartal(2019, 3).plussKvartaler(11)).isEqualTo(new Kvartal(2022, 2));
        assertThat(new Kvartal(2019, 3).plussKvartaler(-1)).isEqualTo(new Kvartal(2019, 2));
    }

    @Test
    public void compareTo_sorter_ÅrstallOgKvartal_først_på_årstall_og_på_kvartal_etterpå() {
        assertThat(new Kvartal(2019, 3).compareTo(new Kvartal(2019, 3))).isEqualTo(0);
        assertThat(new Kvartal(2019, 3).compareTo(new Kvartal(2019, 2))).isEqualTo(1);
        assertThat(new Kvartal(2019, 3).compareTo(new Kvartal(2017, 4))).isEqualTo(1);
        assertThat(new Kvartal(2019, 3).compareTo(new Kvartal(2019, 4))).isEqualTo(-1);
        assertThat(new Kvartal(2019, 3).compareTo(new Kvartal(2020, 1))).isEqualTo(-1);
    }

    @Test
    public void range__skal_returnere_alle_årstall_og_kvartal_mellom_angitt_input() {
        List<Kvartal> liste = Kvartal.range(new Kvartal(2000, 1), new Kvartal(2002, 3));
        assertThat(liste).isEqualTo(Arrays.asList(
                new Kvartal(2000, 1),
                new Kvartal(2000, 2),
                new Kvartal(2000, 3),
                new Kvartal(2000, 4),
                new Kvartal(2001, 1),
                new Kvartal(2001, 2),
                new Kvartal(2001, 3),
                new Kvartal(2001, 4),
                new Kvartal(2002, 1),
                new Kvartal(2002, 2),
                new Kvartal(2002, 3)
        ));
    }

    @Test
    public void range__skal_returnere_tom_liste_hvis_til_er_før_fra() {
        List<Kvartal> liste = Kvartal.range(
                new Kvartal(2004, 1),
                new Kvartal(2002, 1)
        );
        assertThat(liste).isEmpty();
    }
}
