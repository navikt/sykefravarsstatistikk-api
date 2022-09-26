package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori.VIRKSOMHET;

import io.vavr.control.Either;
import java.math.BigDecimal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.MaskerteDataException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.StatistikkException;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SumAvSykefraværOverFlereKvartalerTest {

    @Test
    void kalkulerFraværsprosentMedMaskering_maskererDataHvisAntallTilfellerErUnderFem() {
        Either<StatistikkException, StatistikkDto> maskertSykefravær =
                new SumAvSykefraværOverFlereKvartaler(
                        new UmaskertSykefraværForEttKvartal(
                                new ÅrstallOgKvartal(2022, 1),
                                new BigDecimal(100),
                                new BigDecimal(200),
                                4)
                ).regnUtProsentOgMapTilDto(VIRKSOMHET, "");

        Assertions.assertThat(maskertSykefravær.getLeft())
                .isExactlyInstanceOf(MaskerteDataException.class);
    }


    @Test
    void kalkulerFraværsprosentMedMaskering_returnerProsentHvisAntallTilfellerErFemEllerMer() {
        Either<StatistikkException, StatistikkDto> sykefraværFemTilfeller =
                new SumAvSykefraværOverFlereKvartaler(
                        new UmaskertSykefraværForEttKvartal(
                                new ÅrstallOgKvartal(2022, 1),
                                new BigDecimal(100),
                                new BigDecimal(200),
                                5)
                ).regnUtProsentOgMapTilDto(VIRKSOMHET, "");

        Either<StatistikkException, StatistikkDto> sykefraværTiTilfeller =
                new SumAvSykefraværOverFlereKvartaler(
                        new UmaskertSykefraværForEttKvartal(
                                new ÅrstallOgKvartal(2022, 1),
                                new BigDecimal(100),
                                new BigDecimal(200),
                                10)
                ).regnUtProsentOgMapTilDto(VIRKSOMHET, "");

        Assertions.assertThat(sykefraværFemTilfeller.get().getVerdi()).isEqualTo("50.0");
        Assertions.assertThat(sykefraværTiTilfeller.get().getVerdi()).isEqualTo("50.0");
    }
}
