package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Konstanter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.StatistikkUtils;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;

@Getter
public class SykefraværFlereKvartalerForEksport {

  private BigDecimal tapteDagsverk;
  private BigDecimal muligeDagsverk;
  private BigDecimal prosent;
  private int antallPersoner;
  private List<ÅrstallOgKvartal> kvartaler;
  private final boolean erMaskert;

  public SykefraværFlereKvartalerForEksport(
      List<UmaskertSykefraværForEttKvartal> umaskertSykefravær
  ) {
    erMaskert = umaskertSykefravær.size() != 0 && umaskertSykefravær.stream()
        .allMatch(v -> v.getAntallPersoner()
            < Konstanter.MIN_ANTALL_PERS_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER);

    if (!erMaskert && umaskertSykefravær.size() != 0) {
      tapteDagsverk = umaskertSykefravær.stream()
          .map(UmaskertSykefraværForEttKvartal::getDagsverkTeller)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      muligeDagsverk = umaskertSykefravær.stream()
          .map(UmaskertSykefraværForEttKvartal::getDagsverkNevner)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      prosent = StatistikkUtils.kalkulerSykefraværsprosent(
          this.tapteDagsverk, this.muligeDagsverk).getOrNull();
    } else {
      tapteDagsverk = null;
      muligeDagsverk = null;
      prosent = null;
    }

    kvartaler = umaskertSykefravær.stream()
        .map(UmaskertSykefraværForEttKvartal::getÅrstallOgKvartal)
        .collect(
            Collectors.toList());
    antallPersoner = umaskertSykefravær.size() == 0 ? 0 : umaskertSykefravær.stream()
        .max(Comparator.comparing(UmaskertSykefraværForEttKvartal::getÅrstallOgKvartal)).get()
        .getAntallPersoner();
  }

  public static SykefraværFlereKvartalerForEksport utenStatistikk() {
    return new SykefraværFlereKvartalerForEksport(List.of());
  }
}
