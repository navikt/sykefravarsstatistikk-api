package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2021_1;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2021_2;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.util.List;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import org.junit.jupiter.api.Test;

class SykefraværFlereKvartalerForEksportTest {

  @Test
  public void sjekk_at_prosent_blir_riktig() {
    List<UmaskertSykefraværForEttKvartal> sykefravær = List.of(
        new UmaskertSykefraværForEttKvartal(
            __2021_2,
            new BigDecimal(10),
            new BigDecimal(100),
            8
        ),
        new UmaskertSykefraværForEttKvartal(
            __2021_1,
            new BigDecimal(20),
            new BigDecimal(100),
            8
        )
    );
    SykefraværFlereKvartalerForEksport sykefraværFlereKvartalerForEksport =
        new SykefraværFlereKvartalerForEksport(sykefravær);

    assertThat(sykefraværFlereKvartalerForEksport.getKvartaler().size()).isEqualTo(2);
    assertThat(sykefraværFlereKvartalerForEksport.getProsent()).isEqualByComparingTo(
        new BigDecimal(15));
  }

  @Test
  public void sjekk_at_sykefravær_ikke_blir_maskert() {
    List<UmaskertSykefraværForEttKvartal> sykefravær = List.of(
        new UmaskertSykefraværForEttKvartal(
            __2021_2,
            new BigDecimal(10),
            new BigDecimal(100),
            4
        ),
        new UmaskertSykefraværForEttKvartal(
            __2021_1,
            new BigDecimal(20),
            new BigDecimal(100),
            8
        )
    );

    SykefraværFlereKvartalerForEksport sykefraværFlereKvartalerForEksport =
        new SykefraværFlereKvartalerForEksport(sykefravær);

    assertThat(sykefraværFlereKvartalerForEksport.getTapteDagsverk()).isEqualByComparingTo(
        new BigDecimal(30));
    assertThat(sykefraværFlereKvartalerForEksport.getMuligeDagsverk()).isEqualByComparingTo(
        new BigDecimal(200));
    assertThat(sykefraværFlereKvartalerForEksport.getProsent()).isEqualByComparingTo(
        new BigDecimal(15));

  }

  @Test
  public void sjekk_at_sykefravær_blir_maskert() {
    List<UmaskertSykefraværForEttKvartal> sykefravær = List.of(
        new UmaskertSykefraværForEttKvartal(
            __2021_2,
            new BigDecimal(10),
            new BigDecimal(100),
            4
        ),
        new UmaskertSykefraværForEttKvartal(
            __2021_1,
            new BigDecimal(20),
            new BigDecimal(100),
            4
        )
    );

    SykefraværFlereKvartalerForEksport sykefraværFlereKvartalerForEksport =
        new SykefraværFlereKvartalerForEksport(sykefravær);

    assertThat(sykefraværFlereKvartalerForEksport.getTapteDagsverk()).isNull();
    assertThat(sykefraværFlereKvartalerForEksport.getMuligeDagsverk()).isNull();
    assertThat(sykefraværFlereKvartalerForEksport.getProsent()).isNull();

  }
}
