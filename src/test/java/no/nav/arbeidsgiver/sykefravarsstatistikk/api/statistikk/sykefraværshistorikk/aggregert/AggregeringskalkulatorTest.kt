package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering.Aggregeringskalkulator
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næring
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Sykefraværsdata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.ArbeidsmiljøportalenBransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.BransjeEllerNæring
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class AggregeringskalkulatorTest {
    @Test
    fun fraværsprosentLand_regnerUtRiktigFraværsprosent() {
        val kalkulator = Aggregeringskalkulator(
            Sykefraværsdata(mutableMapOf(Statistikkategori.VIRKSOMHET to   synkendeSykefravær)),
            TestUtils.SISTE_PUBLISERTE_KVARTAL
        )
        AssertionsForClassTypes.assertThat(kalkulator.fraværsprosentVirksomhet("dummynavn").getOrNull()?.verdi)
            .isEqualTo("5.0")
    }

    @Test
    fun fraværsprosentBransjeEllerNæring_regnerUtRiktigFraværsprosentForBransje() {
        val kalkulator = Aggregeringskalkulator(
            Sykefraværsdata(mutableMapOf(Statistikkategori.BRANSJE to   synkendeSykefravær)),
            TestUtils.SISTE_PUBLISERTE_KVARTAL
        )
        val bransje = BransjeEllerNæring(Bransje(ArbeidsmiljøportalenBransje.BARNEHAGER, "Barnehager", listOf("88911")))
        AssertionsForClassTypes.assertThat(kalkulator.fraværsprosentBransjeEllerNæring(bransje).getOrNull()?.verdi)
            .isEqualTo("5.0")
    }

    @Test
    fun fraværsprosentBransjeEllerNæring_regnerUtRiktigFraværsprosentForNæring() {
        val kalkulator = Aggregeringskalkulator(
            Sykefraværsdata(mutableMapOf(Statistikkategori.NÆRING to synkendeSykefravær)),
            TestUtils.SISTE_PUBLISERTE_KVARTAL
        )
        val dummynæring = BransjeEllerNæring(Næring("00"))
        AssertionsForClassTypes.assertThat(kalkulator.fraværsprosentBransjeEllerNæring(dummynæring).getOrNull()?.verdi)
            .isEqualTo("5.0")
    }

    @Test
    fun fraværsprosentNorge_regnerUtRiktigFraværsprosent() {
        val kalkulator = Aggregeringskalkulator(
            Sykefraværsdata(mutableMapOf(Statistikkategori.LAND to synkendeSykefravær)),
            TestUtils.SISTE_PUBLISERTE_KVARTAL
        )
        AssertionsForClassTypes.assertThat(kalkulator.fraværsprosentNorge().getOrNull()?.verdi).isEqualTo("5.0")
    }

    @Test
    fun trendBransjeEllerNæring_regnerUtRiktigTrendForNæring() {
        val kalkulator = Aggregeringskalkulator(
            Sykefraværsdata(mutableMapOf(Statistikkategori.NÆRING to synkendeSykefravær)),
            TestUtils.SISTE_PUBLISERTE_KVARTAL
        )
        val dummynæring = BransjeEllerNæring(Næring("00"))
        AssertionsForClassTypes.assertThat(kalkulator.trendBransjeEllerNæring(dummynæring).getOrNull()?.verdi)
            .isEqualTo("-8.0")
    }

    private val synkendeSykefravær = listOf(
        UmaskertSykefraværForEttKvartal(
            TestUtils.sisteKvartalMinus(0),
            BigDecimal.valueOf(2),
            BigDecimal.valueOf(100),
            10
        ),
        UmaskertSykefraværForEttKvartal(
            TestUtils.sisteKvartalMinus(1),
            BigDecimal.valueOf(4),
            BigDecimal.valueOf(100),
            10
        ),
        UmaskertSykefraværForEttKvartal(
            TestUtils.sisteKvartalMinus(2),
            BigDecimal.valueOf(6),
            BigDecimal.valueOf(100),
            10
        ),
        UmaskertSykefraværForEttKvartal(
            TestUtils.sisteKvartalMinus(3),
            BigDecimal.valueOf(8),
            BigDecimal.valueOf(100),
            10
        ),
        UmaskertSykefraværForEttKvartal(
            TestUtils.sisteKvartalMinus(4),
            BigDecimal.valueOf(10),
            BigDecimal.valueOf(100),
            10
        )
    )
}
