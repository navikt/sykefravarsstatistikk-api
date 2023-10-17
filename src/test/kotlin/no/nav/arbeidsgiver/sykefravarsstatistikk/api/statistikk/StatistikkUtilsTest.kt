package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering.Statistikkfeil
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils.StatistikkUtils.kalkulerSykefraværsprosent
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class StatistikkUtilsTest {
    @Test
    fun kalkulerSykefraværsprosent_girÉnDesimalISvaret() {
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal.ONE, BigDecimal.ONE).getOrNull())
            .isEqualTo(BigDecimal("100.0"))
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal.ONE, BigDecimal("1.00")).getOrNull())
            .isEqualTo(BigDecimal("100.0"))
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal("1.000"), BigDecimal("1.0")).getOrNull())
            .isEqualTo(BigDecimal("100.0"))
    }

    @Test
    fun kalkulerSykefraværsprosent_regnerUtKorrektProsent() {
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal("5"), BigDecimal("10")).getOrNull())
            .isEqualTo(BigDecimal("50.0"))
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal.ZERO, BigDecimal("100.00")).getOrNull())
            .isEqualTo(BigDecimal("0.0"))
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal("0.01"), BigDecimal("1000")).getOrNull())
            .isEqualTo(BigDecimal("0.0"))
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal("1.01"), BigDecimal("1.02")).getOrNull())
            .isEqualTo(BigDecimal("99.0"))
    }

    @Test
    fun kalkulerSykefraværsprosent_returnererStatistikkException_dersomNevnerErZero() {
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal("5"), BigDecimal.ZERO).swap().getOrNull())
            .isInstanceOf(Statistikkfeil::class.java)
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal("5"), BigDecimal("0.0")).swap().getOrNull())
            .isInstanceOf(Statistikkfeil::class.java)
    }

    @Test
    fun kalkulerSykefraværsprosent_runderOppKorrekt() {
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal("0.55"), BigDecimal.ONE).getOrNull())
            .isEqualTo(BigDecimal("55.0"))
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal("0.555"), BigDecimal.ONE).getOrNull())
            .isEqualTo(BigDecimal("55.5"))
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal("0.5555"), BigDecimal.ONE).getOrNull())
            .isEqualTo(BigDecimal("55.6"))
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal("0.05"), BigDecimal.ONE).getOrNull())
            .isEqualTo(BigDecimal("5.0"))
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal("0.005"), BigDecimal.ONE).getOrNull())
            .isEqualTo(BigDecimal("0.5"))
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal("0.0005"), BigDecimal.ONE).getOrNull())
            .isEqualTo(BigDecimal("0.1"))
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal("0.00005"), BigDecimal.ONE).getOrNull())
            .isEqualTo(BigDecimal("0.0"))
    }

    @Test
    fun kalkulerSykefraværsprosent_runderNedKorrekt() {
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal("0.44"), BigDecimal.ONE).getOrNull())
            .isEqualTo(BigDecimal("44.0"))
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal("0.444"), BigDecimal.ONE).getOrNull())
            .isEqualTo(BigDecimal("44.4"))
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal("0.4444"), BigDecimal.ONE).getOrNull())
            .isEqualTo(BigDecimal("44.4"))
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal("0.04"), BigDecimal.ONE).getOrNull())
            .isEqualTo(BigDecimal("4.0"))
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal("0.004"), BigDecimal.ONE).getOrNull())
            .isEqualTo(BigDecimal("0.4"))
        Assertions.assertThat(kalkulerSykefraværsprosent(BigDecimal("0.0004"), BigDecimal.ONE).getOrNull())
            .isEqualTo(BigDecimal("0.0"))
    }
}
