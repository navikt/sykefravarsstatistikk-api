package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Konstanter.*;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.StatistikkUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@EqualsAndHashCode
public abstract class MaskerbartSykefravær {

  public final BigDecimal prosent;
  public final BigDecimal tapteDagsverk;
  public final BigDecimal muligeDagsverk;
  public final boolean erMaskert;

  public MaskerbartSykefravær(
      BigDecimal tapteDagsverk,
      BigDecimal muligeDagsverk,
      int antallPersoner,
      boolean harSykefraværData) {
    erMaskert =
        harSykefraværData
            && antallPersoner < MIN_ANTALL_PERS_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER;
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
