package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.SykefraværFlereKvartalerForEksport
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.UmaskertSykefraværForEttKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2021_1
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.EksporteringServiceTestUtils.__2021_2
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class SykefraværFlereKvartalerForEksportTest {
    @Test
    fun sjekk_at_prosent_blir_riktig() {
        val sykefravær = listOf(
            UmaskertSykefraværForEttKvartal(
                __2021_2, BigDecimal(10), BigDecimal(100), 8
            ),
            UmaskertSykefraværForEttKvartal(
                __2021_1, BigDecimal(20), BigDecimal(100), 8
            )
        )
        val sykefraværFlereKvartalerForEksport = SykefraværFlereKvartalerForEksport(sykefravær)
        AssertionsForClassTypes.assertThat(sykefraværFlereKvartalerForEksport.kvartaler.size).isEqualTo(2)
        AssertionsForClassTypes.assertThat(sykefraværFlereKvartalerForEksport.prosent)
            .isEqualByComparingTo(BigDecimal(15))
    }

    @Test
    fun sjekk_at_sykefravær_ikke_blir_maskert() {
        val sykefravær = listOf(
            UmaskertSykefraværForEttKvartal(
                __2021_2, BigDecimal(10), BigDecimal(100), 4
            ),
            UmaskertSykefraværForEttKvartal(
                __2021_1, BigDecimal(20), BigDecimal(100), 8
            )
        )
        val sykefraværFlereKvartalerForEksport = SykefraværFlereKvartalerForEksport(sykefravær)
        AssertionsForClassTypes.assertThat(sykefraværFlereKvartalerForEksport.tapteDagsverk)
            .isEqualByComparingTo(BigDecimal(30))
        AssertionsForClassTypes.assertThat(sykefraværFlereKvartalerForEksport.muligeDagsverk)
            .isEqualByComparingTo(BigDecimal(200))
        AssertionsForClassTypes.assertThat(sykefraværFlereKvartalerForEksport.prosent)
            .isEqualByComparingTo(BigDecimal(15))
    }

    @Test
    fun sjekk_at_sykefravær_blir_maskert() {
        val sykefravær = listOf(
            UmaskertSykefraværForEttKvartal(
                __2021_2, BigDecimal(10), BigDecimal(100), 4
            ),
            UmaskertSykefraværForEttKvartal(
                __2021_1, BigDecimal(20), BigDecimal(100), 4
            )
        )
        val sykefraværFlereKvartalerForEksport = SykefraværFlereKvartalerForEksport(sykefravær)
        AssertionsForClassTypes.assertThat(sykefraværFlereKvartalerForEksport.tapteDagsverk).isNull()
        AssertionsForClassTypes.assertThat(sykefraværFlereKvartalerForEksport.muligeDagsverk).isNull()
        AssertionsForClassTypes.assertThat(sykefraværFlereKvartalerForEksport.prosent).isNull()
    }

    @Test
    fun kan_være_tom_for_statistikk() {
        val sykefravær = listOf<UmaskertSykefraværForEttKvartal>()
        val sykefraværFlereKvartalerForEksport = SykefraværFlereKvartalerForEksport(sykefravær)
        AssertionsForClassTypes.assertThat(sykefraværFlereKvartalerForEksport.tapteDagsverk).isNull()
        AssertionsForClassTypes.assertThat(sykefraværFlereKvartalerForEksport.muligeDagsverk).isNull()
        AssertionsForClassTypes.assertThat(sykefraværFlereKvartalerForEksport.prosent).isNull()
        AssertionsForClassTypes.assertThat(sykefraværFlereKvartalerForEksport.erMaskert).isFalse()
    }
}
