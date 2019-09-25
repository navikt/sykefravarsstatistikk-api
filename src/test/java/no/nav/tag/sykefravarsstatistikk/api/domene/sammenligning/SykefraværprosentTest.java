package no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class SykefraværprosentTest {
    @Test
    public void sykefraværprosent__skal_regne_ut_riktig_prosent_ut_i_fra_tapte_og_mulige_dagsverk() {
        Sykefraværprosent sykefraværprosent = new Sykefraværprosent("", new BigDecimal(5), new BigDecimal(10));
        assertAlmostEqual(sykefraværprosent.getProsent(), 50);
    }

    @Test
    public void test() {
        BigDecimal muligeDagsverk = new BigDecimal(131259560.164769);
        BigDecimal tapteDagsverk = new BigDecimal(6388935.367072);
        assertEquals(
                "4.868",
                tapteDagsverk.multiply(
                        new BigDecimal(100)).divide(
                        muligeDagsverk,
                        3,
                        RoundingMode.UP)
                        .toString()
        );
/*
        BigDecimal sfProsent = LandStatistikk.builder()
                .arstall(2016)
                .kvartal(2)
                .muligeDagsverk(muligeDagsverk)
                .tapteDagsverk(tapteDagsverk)
                .build()
                .beregnSykkefravarProsent();

        assertEquals("En desimal", "4.9", sfProsent.toString());*/
    }

    private void assertAlmostEqual(BigDecimal tall1, double tall2) {
        BigDecimal feilmargin = new BigDecimal("1").scaleByPowerOfTen(-15);
        BigDecimal forskjellMellomTallene = tall1.subtract(new BigDecimal(tall2)).abs();
        assertThat(forskjellMellomTallene).isLessThanOrEqualTo(feilmargin);
    }
}
