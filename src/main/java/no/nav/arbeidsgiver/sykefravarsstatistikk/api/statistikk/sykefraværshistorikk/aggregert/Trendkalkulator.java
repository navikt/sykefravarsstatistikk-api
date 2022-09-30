package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal.hentUtKvartal;

import io.vavr.control.Either;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UtilstrekkeligDataException;

@AllArgsConstructor
class Trendkalkulator {

  List<UmaskertSykefraværForEttKvartal> datagrunnlag;
  ÅrstallOgKvartal sistePubliserteKvartal;


  Either<UtilstrekkeligDataException, Trend> kalkulerTrend() {

    ÅrstallOgKvartal ettÅrSiden = sistePubliserteKvartal.minusEttÅr();

    Optional<UmaskertSykefraværForEttKvartal> nyesteSykefravær =
        hentUtKvartal(datagrunnlag, sistePubliserteKvartal);
    Optional<UmaskertSykefraværForEttKvartal> sykefraværetEttÅrSiden =
        hentUtKvartal(datagrunnlag, ettÅrSiden);

    if (nyesteSykefravær.isEmpty() || sykefraværetEttÅrSiden.isEmpty()) {
      return Either.left(
          new UtilstrekkeligDataException(
              "Mangler data for " + sistePubliserteKvartal + " og/eller " + ettÅrSiden));
    }

    if (nyesteSykefravær.get().harAntallMuligeDagsverkLikNull() ||
        sykefraværetEttÅrSiden.get().harAntallMuligeDagsverkLikNull()) {
      return Either.left(new UtilstrekkeligDataException(
          "Kan ikke regne ut sykefraværsprosent når antall mulige dagsverk er null."));
    }

    BigDecimal trendverdi = nyesteSykefravær.get().kalkulerSykefraværsprosent()
        .subtract(sykefraværetEttÅrSiden.get().kalkulerSykefraværsprosent());

    int antallTilfeller =
        nyesteSykefravær.get().getAntallPersoner()
            + sykefraværetEttÅrSiden.get().getAntallPersoner();

    return Either.right(
        new Trend(trendverdi, antallTilfeller, List.of(sistePubliserteKvartal, ettÅrSiden)));
  }
}
