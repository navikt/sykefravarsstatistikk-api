package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk

import io.kotest.matchers.equals.shouldBeEqual
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.SumAvSykefraværOverFlereKvartaler
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.SumAvSykefraværOverFlereKvartaler.MaskertDataFeil
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class SumAvSykefraværOverFlereKvartalerTest {
    @Test
    fun leggSammenToKvartaler() {
        val umaskertSykefravær_Q1 = UmaskertSykefraværForEttKvartal(
            ÅrstallOgKvartal(2022, 1), BigDecimal(100), BigDecimal(200), 5
        )
        val umaskertSykefravær_Q2 = UmaskertSykefraværForEttKvartal(
            ÅrstallOgKvartal(2022, 2), BigDecimal(10), BigDecimal(230), 5
        )

        val kvartal1 = SumAvSykefraværOverFlereKvartaler(umaskertSykefravær_Q1)
        val kvartal2 = SumAvSykefraværOverFlereKvartaler(umaskertSykefravær_Q2)

        val result = kvartal1.leggSammen(kvartal2)

        result.muligeDagsverk shouldBeEqual BigDecimal("430.0")
        result.tapteDagsverk shouldBeEqual  BigDecimal("110.0")
    }

    @Test
    fun kalkulerFraværsprosentMedMaskering_maskererDataHvisAntallTilfellerErUnderFem() {
        val maskertSykefravær = SumAvSykefraværOverFlereKvartaler(
            UmaskertSykefraværForEttKvartal(
                ÅrstallOgKvartal(2022, 1), BigDecimal(100), BigDecimal(200), 4
            )
        )
            .regnUtProsentOgMapTilDto(Statistikkategori.VIRKSOMHET, "")
        Assertions.assertThat(maskertSykefravær.swap().getOrNull())
            .isExactlyInstanceOf(MaskertDataFeil::class.java)
    }

    @Test
    fun kalkulerFraværsprosentMedMaskering_returnerProsentHvisAntallTilfellerErFemEllerMer() {
        val sykefraværFemTilfeller = SumAvSykefraværOverFlereKvartaler(
            UmaskertSykefraværForEttKvartal(
                ÅrstallOgKvartal(2022, 1), BigDecimal(100), BigDecimal(200), 5
            )
        )
            .regnUtProsentOgMapTilDto(Statistikkategori.VIRKSOMHET, "")
        val sykefraværTiTilfeller = SumAvSykefraværOverFlereKvartaler(
            UmaskertSykefraværForEttKvartal(
                ÅrstallOgKvartal(2022, 1), BigDecimal(100), BigDecimal(200), 10
            )
        )
            .regnUtProsentOgMapTilDto(Statistikkategori.VIRKSOMHET, "")
        Assertions.assertThat(sykefraværFemTilfeller.getOrNull()!!.verdi).isEqualTo("50.0")
        Assertions.assertThat(sykefraværTiTilfeller.getOrNull()!!.verdi).isEqualTo("50.0")
    }
}
