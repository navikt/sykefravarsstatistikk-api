package no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning;

import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class SykefraværprosentTest {
    @Test
    public void sykefraværprosent__skal_regne_ut_riktig_prosent_ut_i_fra_tapte_og_mulige_dagsverk() {
        Sykefraværprosent sykefraværprosent = new Sykefraværprosent("", new BigDecimal(5), new BigDecimal(10), 20);
        assertThat(sykefraværprosent.getProsent()).isEqualTo(new BigDecimal("50.0"));
    }

    @Test
    public void sykefraværprosent__skal_runde_prosenten_opp_ved_tvil() {
        Sykefraværprosent sykefraværprosent = new Sykefraværprosent("", new BigDecimal(455), new BigDecimal(10000), 100);
        assertThat(sykefraværprosent.getProsent()).isEqualTo(new BigDecimal("4.6"));
    }

    @Test
    public void sykefraværprosent__skal_være_maskert_hvis_antallPersoner_er_4_eller_under() {
        Sykefraværprosent sykefraværprosent = new Sykefraværprosent("", new BigDecimal(1), new BigDecimal(10), 4);
        assertThat(sykefraværprosent.isErMaskert()).isTrue();
        assertThat(sykefraværprosent.getProsent()).isNull();
    }

    @Test
    public void sykefraværprosent__skal_være_maskert_hvis_antallPersoner_over_4() {
        Sykefraværprosent sykefraværprosent = new Sykefraværprosent("", new BigDecimal(1), new BigDecimal(10), 5);
        assertThat(sykefraværprosent.isErMaskert()).isFalse();
        assertThat(sykefraværprosent.getProsent()).isNotNull();
    }
}
