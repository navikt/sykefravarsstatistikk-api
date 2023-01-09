package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.StatistikkUtils;

@Getter
@EqualsAndHashCode
public abstract class MaskerbartSykefraværOverFlereKvartaler {

  private final BigDecimal prosent;
  private final BigDecimal tapteDagsverk;
  private final BigDecimal muligeDagsverk;
  private final boolean erMaskert;

  public MaskerbartSykefraværOverFlereKvartaler(
      BigDecimal tapteDagsverk,
      BigDecimal muligeDagsverk,
      List<SykefraværForEttKvartal> sykefraværForEttKvartalList,
      boolean harSykefraværData) {
    erMaskert =
        harSykefraværData
            && sykefraværForEttKvartalList.stream().allMatch(MaskerbartSykefravær::isErMaskert);

    if (!erMaskert && harSykefraværData) {
      prosent =
          StatistikkUtils.kalkulerSykefraværsprosent(tapteDagsverk, muligeDagsverk).getOrNull();

      this.tapteDagsverk = tapteDagsverk.setScale(1, RoundingMode.HALF_UP);
      this.muligeDagsverk = muligeDagsverk.setScale(1, RoundingMode.HALF_UP);
    } else {
      prosent = null;
      this.tapteDagsverk = null;
      this.muligeDagsverk = null;
    }
  }
}
