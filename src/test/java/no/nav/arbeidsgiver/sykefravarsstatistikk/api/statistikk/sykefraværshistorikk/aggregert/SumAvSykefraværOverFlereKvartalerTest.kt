package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SumAvSykefraværOverFlereKvartaler.MaskerteDataException
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
        val expected = SykefraværOverFlereKvartaler(
            listOf(
                umaskertSykefravær_Q1.årstallOgKvartal,
                umaskertSykefravær_Q2.årstallOgKvartal
            ),
            BigDecimal(110),
            BigDecimal(430),
            listOf(
                SykefraværForEttKvartal(
                    umaskertSykefravær_Q1.årstallOgKvartal,
                    umaskertSykefravær_Q1.dagsverkTeller,
                    umaskertSykefravær_Q1.dagsverkNevner,
                    umaskertSykefravær_Q1.antallPersoner
                ),
                SykefraværForEttKvartal(
                    umaskertSykefravær_Q2.årstallOgKvartal,
                    umaskertSykefravær_Q2.dagsverkTeller,
                    umaskertSykefravær_Q2.dagsverkNevner,
                    umaskertSykefravær_Q2.antallPersoner
                )
            )
        )
        val kvartal1 = SumAvSykefraværOverFlereKvartaler(umaskertSykefravær_Q1)
        val kvartal2 = SumAvSykefraværOverFlereKvartaler(umaskertSykefravær_Q2)
        val result = kvartal1.leggSammen(kvartal2)
        val resultSykefraværOverFlereKvartaler = result.regnUtProsentOgMapTilSykefraværForFlereKvartaler().getOrNull()
        Assertions.assertThat(result.muligeDagsverk).isEqualByComparingTo(BigDecimal(430))
        Assertions.assertThat(result.tapteDagsverk).isEqualByComparingTo(BigDecimal(110))
        Assertions.assertThat(resultSykefraværOverFlereKvartaler!!.prosent)
            .isEqualByComparingTo(expected.prosent)
        Assertions.assertThat(resultSykefraværOverFlereKvartaler.kvartaler)
            .isEqualTo(expected.kvartaler)
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
            .isExactlyInstanceOf(MaskerteDataException::class.java)
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
