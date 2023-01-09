package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk;

import io.vavr.control.Either;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.StatistikkException;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class StatistikkUtils {

  /**
   * Source of trouth for kalkulering av sykefraværsprosent basert på tapte dagsverk og mulige
   * dagsverk.
   */
  public static Either<StatistikkException, BigDecimal> kalkulerSykefraværsprosent(
      @NotNull BigDecimal dagsverkTeller, @NotNull BigDecimal dagsverkNevner) {

    final int ANTALL_SIFRE_I_UTREGNING = 3;
    final int ANTALL_SIFRE_I_RESULTAT = 1;

    try {
      return Either.right(
          dagsverkTeller
              .divide(dagsverkNevner, ANTALL_SIFRE_I_UTREGNING, RoundingMode.HALF_UP)
              .multiply(new BigDecimal(100))
              .setScale(ANTALL_SIFRE_I_RESULTAT, RoundingMode.HALF_UP));
    } catch (ArithmeticException e) {
      return Either.left(
          new StatistikkException(
              "Kan ikke regne ut prosent når antall dagsverk i nevner er lik " + dagsverkNevner));
    }
  }
}
