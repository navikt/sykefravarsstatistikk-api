package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertApi.Trendkalkulator
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertApi.domene.Trend
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertApi.UtilstrekkeligData
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class TrendkalkulatorTest {
    @Test
    fun kalkulerTrend_returnererManglendeDataException_nårEtKvartalMangler() {
        Assertions.assertThat(
            Trendkalkulator(
                listOf(
                    umaskertSykefravær(ÅrstallOgKvartal(2021, 2), 11.0, 1),
                    umaskertSykefravær(ÅrstallOgKvartal(2021, 1), 10.0, 3)
                ),
                TestUtils.SISTE_PUBLISERTE_KVARTAL
            )
                .kalkulerTrend()
                .swap().getOrNull()
        )
            .isExactlyInstanceOf(UtilstrekkeligData::class.java)
    }

    @Test
    fun kalkulerTrend_returnererPositivTrend_dersomSykefraværetØker() {
        val k1 = TestUtils.SISTE_PUBLISERTE_KVARTAL
        val k2 = TestUtils.SISTE_PUBLISERTE_KVARTAL.minusEttÅr()
        Assertions.assertThat(
            Trendkalkulator(
                listOf(umaskertSykefravær(k1, 3.0, 10), umaskertSykefravær(k2, 2.0, 10)),
                TestUtils.SISTE_PUBLISERTE_KVARTAL
            )
                .kalkulerTrend()
                .getOrNull()
        )
            .isEqualTo(Trend(BigDecimal("1.0"), 20, listOf(k1, k2)))
    }

    @Test
    fun kalkulerTrend_returnereNegativTrend_dersomSykefraværetMinker() {
        val kvartalstall = listOf(
            umaskertSykefravær(TestUtils.SISTE_PUBLISERTE_KVARTAL, 8.0, 1),
            umaskertSykefravær(ÅrstallOgKvartal(2020, 2), 13.0, 2),
            umaskertSykefravær(TestUtils.SISTE_PUBLISERTE_KVARTAL.minusEttÅr(), 10.0, 3)
        )
        val forventetTrend = Trend(
            BigDecimal("-2.0"),
            4,
            listOf(TestUtils.SISTE_PUBLISERTE_KVARTAL, TestUtils.SISTE_PUBLISERTE_KVARTAL.minusEttÅr())
        )
        Assertions.assertThat(
            Trendkalkulator(kvartalstall, TestUtils.SISTE_PUBLISERTE_KVARTAL).kalkulerTrend().getOrNull()
        )
            .isEqualTo(forventetTrend)
    }

    @Test
    fun kalkulerTrend_girUtrilstrekkeligDataException_vedTomtDatagrunnlag() {
        Assertions.assertThat(
            Trendkalkulator(listOf(), TestUtils.SISTE_PUBLISERTE_KVARTAL).kalkulerTrend().swap().getOrNull()
        )
            .isExactlyInstanceOf(UtilstrekkeligData::class.java)
    }

    companion object {
        private fun umaskertSykefravær(
            årstallOgKvartal: ÅrstallOgKvartal, tapteDagsverk: Double, antallPersoner: Int
        ): UmaskertSykefraværForEttKvartal {
            return UmaskertSykefraværForEttKvartal(
                årstallOgKvartal, BigDecimal(tapteDagsverk), BigDecimal(100), antallPersoner
            )
        }
    }
}
