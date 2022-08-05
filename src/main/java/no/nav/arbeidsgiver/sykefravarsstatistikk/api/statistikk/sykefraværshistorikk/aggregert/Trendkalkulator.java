package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.sisteKvartal;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal.hentUtKvartal;

import io.vavr.control.Either;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UtilstrekkeligDataException;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
class Trendkalkulator {

    List<UmaskertSykefraværForEttKvartal> datagrunnlag;

    Either<UtilstrekkeligDataException, Trend> kalkulerTrend() {
        Optional<UmaskertSykefraværForEttKvartal> nyesteSykefravær =
              hentUtKvartal(datagrunnlag, sisteKvartal());
        Optional<UmaskertSykefraværForEttKvartal> sykefraværetEtÅrSiden =
              hentUtKvartal(datagrunnlag, sisteKvartal().minusEttÅr());

        if (nyesteSykefravær.isEmpty() || sykefraværetEtÅrSiden.isEmpty()) {
            return Either.left(
                  new UtilstrekkeligDataException(
                        "Mangler data for " + sisteKvartal() +
                              " og/eller " + sisteKvartal().minusEttÅr()));
        }

        BigDecimal trendverdi = nyesteSykefravær.get().getSykefraværsprosent()
              .subtract(sykefraværetEtÅrSiden.get().getSykefraværsprosent());

        int antallTilfeller =
              nyesteSykefravær.get().getAntallPersoner()
                    + sykefraværetEtÅrSiden.get().getAntallPersoner();

        return Either.right(
              new Trend(
                    trendverdi,
                    antallTilfeller,
                    List.of(sisteKvartal(), sisteKvartal().minusEttÅr())
              ));
    }
}