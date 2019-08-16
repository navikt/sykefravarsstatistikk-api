package no.nav.tag.sykefravarsstatistikk.api.domain.stats;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.Assert.assertEquals;

public class LandStatistikkTest {

    @Test
    public void sykefravar_prosent_har_et_desimaltall_som_rundes_oppover() {
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

        BigDecimal sfProsent = LandStatistikk.builder()
                .arstall(2016)
                .kvartal(2)
                .muligeDagsverk(muligeDagsverk)
                .tapteDagsverk(tapteDagsverk)
                .build()
                .beregnSykkefravarProsent();

        assertEquals("En desimal", "4.9", sfProsent.toString());
    }
}