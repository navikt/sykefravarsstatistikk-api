package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.oppsummert;

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
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.ManglendeDataException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
class TrendKalkulator {

    private static final ÅrstallOgKvartal nyesteKvartal = SISTE_PUBLISERTE_KVARTAL;
    private static final ÅrstallOgKvartal ettÅrTidligere =
            SISTE_PUBLISERTE_KVARTAL.minusEttÅr();
    public BigDecimal trendverdi;
    public int antallTilfellerIBeregningen;
    public List<ÅrstallOgKvartal> kvartalerIBeregningen;

    public static Either<ManglendeDataException, TrendKalkulator> kalkulerTrend(
            List<UmaskertSykefraværForEttKvartal> sykefravær) {

        Optional<UmaskertSykefraværForEttKvartal> nyesteSykefravær =
                hentUtKvartal(sykefravær, nyesteKvartal);
        Optional<UmaskertSykefraværForEttKvartal> sykefraværetEtÅrSiden =
                hentUtKvartal(sykefravær, ettÅrTidligere);

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
                new TrendKalkulator(
                        trendverdi,
                        antallTilfeller,
                        List.of(SISTE_PUBLISERTE_KVARTAL, SISTE_PUBLISERTE_KVARTAL.minusEttÅr())
                ));
    }

    public OppsummertStatistikkDto tilOppsummertStatistikkDto(
            Statistikkategori type, String label) {
        return new OppsummertStatistikkDto(
                type,
                label,
                this.trendverdi.toString(),
                this.antallTilfellerIBeregningen,
                this.kvartalerIBeregningen);
    }
}
