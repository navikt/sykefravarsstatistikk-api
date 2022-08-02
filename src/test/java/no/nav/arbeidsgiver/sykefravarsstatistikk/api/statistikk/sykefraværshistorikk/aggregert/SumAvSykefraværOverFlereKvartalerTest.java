package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import io.vavr.control.Either;
import java.math.BigDecimal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.StatistikkException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SumAvSykefraværOverFlereKvartalerTest {

    @Test
    void kalkulerFraværsprosentMedMaskering_maskererDataHvisAntallTilfellerErUnderFem() {
        Either<StatistikkException, BigDecimal> maskertSykefravær = new SumAvSykefraværOverFlereKvartaler(
                new UmaskertSykefraværForEttKvartal(
                        new ÅrstallOgKvartal(2022, 1),
                        new BigDecimal(100),
                        new BigDecimal(200),
                        4)
        ).kalkulerFraværsprosentMedMaskering();

        Assertions.assertThat(maskertSykefravær.getLeft())
                .isExactlyInstanceOf(MaskerteDataException.class);
    }

    @Test
    void kalkulerFraværsprosentMedMaskering_returnerProsentHvisAntallTilfellerErFemEllerMer() {
        Either<StatistikkException, BigDecimal> sykefraværFemTilfeller = new SumAvSykefraværOverFlereKvartaler(
                new UmaskertSykefraværForEttKvartal(
                        new ÅrstallOgKvartal(2022, 1),
                        new BigDecimal(100),
                        new BigDecimal(200),
                        5)
        ).kalkulerFraværsprosentMedMaskering();

        Either<StatistikkException, BigDecimal> sykefraværTiTilfeller = new SumAvSykefraværOverFlereKvartaler(
                new UmaskertSykefraværForEttKvartal(
                        new ÅrstallOgKvartal(2022, 1),
                        new BigDecimal(100),
                        new BigDecimal(200),
                        10)
        ).kalkulerFraværsprosentMedMaskering();

        Assertions.assertThat(sykefraværFemTilfeller.get())
                .isEqualTo(new BigDecimal("50.00"));
        Assertions.assertThat(sykefraværTiTilfeller.get())
                .isEqualTo(new BigDecimal("50.00"));
    }
}