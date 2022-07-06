package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.SISTE_PUBLISERTE_KVARTAL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal.hentUtKvartal;

import io.vavr.control.Either;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.ManglendeDataException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
class Trendkalkulator {

    private static final ÅrstallOgKvartal nyesteKvartal = SISTE_PUBLISERTE_KVARTAL;
    private static final ÅrstallOgKvartal ettÅrTidligere =
            SISTE_PUBLISERTE_KVARTAL.minusEttÅr();
    List<UmaskertSykefraværForEttKvartal> datagrunnlag;


    Either<ManglendeDataException, Trend> kalkulerTrend() {

        Optional<UmaskertSykefraværForEttKvartal> nyesteSykefravær =
                hentUtKvartal(datagrunnlag, nyesteKvartal);
        Optional<UmaskertSykefraværForEttKvartal> sykefraværetEtÅrSiden =
                hentUtKvartal(datagrunnlag, ettÅrTidligere);

        if (nyesteSykefravær.isEmpty() || sykefraværetEtÅrSiden.isEmpty()) {
            return Either.left(
                    new ManglendeDataException(
                            "Mangler " + nyesteKvartal + " eller " + ettÅrTidligere));
        }

        BigDecimal trendverdi = nyesteSykefravær.get().getProsent()
                .subtract(sykefraværetEtÅrSiden.get().getProsent());

        int antallTilfeller =
                nyesteSykefravær.get().getAntallPersoner()
                        + sykefraværetEtÅrSiden.get().getAntallPersoner();

        return Either.right(
                new Trend(
                        trendverdi,
                        antallTilfeller,
                        List.of(nyesteKvartal, ettÅrTidligere)
                ));
    }
}