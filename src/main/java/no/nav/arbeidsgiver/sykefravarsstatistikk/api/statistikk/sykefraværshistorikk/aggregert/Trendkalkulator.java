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
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UtilstrekkeligDataException;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
class Trendkalkulator {

    List<UmaskertSykefraværForEttKvartal> datagrunnlag;

    Either<UtilstrekkeligDataException, Trend> kalkulerTrend() {

        ÅrstallOgKvartal sisteKvartal = sisteKvartal();
        ÅrstallOgKvartal ettÅrSiden = sisteKvartal.minusEttÅr();

        Optional<UmaskertSykefraværForEttKvartal> nyesteSykefravær =
              hentUtKvartal(datagrunnlag, sisteKvartal);
        Optional<UmaskertSykefraværForEttKvartal> sykefraværetEttÅrSiden =
              hentUtKvartal(datagrunnlag, ettÅrSiden);

        if (nyesteSykefravær.isEmpty() || sykefraværetEttÅrSiden.isEmpty()) {
            return Either.left(
                  new UtilstrekkeligDataException(
                        "Mangler data for " + sisteKvartal + " og/eller " + ettÅrSiden));
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
              new Trend(trendverdi, antallTilfeller, List.of(sisteKvartal, ettÅrSiden)));
    }
}