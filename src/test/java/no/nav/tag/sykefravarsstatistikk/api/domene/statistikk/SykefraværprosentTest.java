package no.nav.tag.sykefravarsstatistikk.api.domene.statistikk;

import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class SykefraværprosentTest {
    @Test
    public void sykefraværprosent__skal_regne_ut_riktig_prosent_ut_i_fra_tapte_og_mulige_dagsverk() {
        Sykefraværprosent sykefraværprosent = new Sykefraværprosent("", new BigDecimal(5), new BigDecimal(10));
        assertAlmostEqual(sykefraværprosent.getProsent(), 50);
    }

    private void assertAlmostEqual(BigDecimal tall1, double tall2) {
        BigDecimal feilmargin = new BigDecimal("1").scaleByPowerOfTen(-15);
        BigDecimal forskjellMellomTallene = tall1.subtract(new BigDecimal(tall2)).abs();
        assertThat(forskjellMellomTallene).isLessThanOrEqualTo(feilmargin);
    }
}
