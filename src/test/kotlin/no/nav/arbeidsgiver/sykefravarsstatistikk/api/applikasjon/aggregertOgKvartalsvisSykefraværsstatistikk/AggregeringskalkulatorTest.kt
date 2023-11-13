package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk

import ia.felles.definisjoner.bransjer.Bransje
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregertOgKvartalsvisSykefraværsstatistikk.domene.Sykefraværsdata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.Test
import testUtils.TestUtils
import java.math.BigDecimal

internal class AggregeringskalkulatorTest {
    @Test
    fun fraværsprosentLand_regnerUtRiktigFraværsprosent() {
        val virksomhet =
            Underenhet.Næringsdrivende(Orgnr("123456789"), Orgnr("123456789"), "dummynavn", Næringskode("11111"), 5)
        val kalkulator = Aggregeringskalkulator(
            Sykefraværsdata(mapOf(Aggregeringskategorier.Virksomhet(virksomhet) to synkendeSykefravær)),
            TestUtils.SISTE_PUBLISERTE_KVARTAL
        )
        AssertionsForClassTypes.assertThat(kalkulator.fraværsprosentVirksomhet("dummynavn").getOrNull()?.verdi)
            .isEqualTo("5.0")
    }

    @Test
    fun fraværsprosentBransjeEllerNæring_regnerUtRiktigFraværsprosentForBransje() {
        val kalkulator = Aggregeringskalkulator(
            Sykefraværsdata(mapOf(Aggregeringskategorier.Bransje(Bransje.BARNEHAGER) to synkendeSykefravær)),
            TestUtils.SISTE_PUBLISERTE_KVARTAL
        )
        val bransjeEllerNæring = BransjeEllerNæring(Bransje.BARNEHAGER)
        AssertionsForClassTypes.assertThat(
            kalkulator.fraværsprosentBransjeEllerNæring(bransjeEllerNæring).getOrNull()?.verdi
        )
            .isEqualTo("5.0")
    }

    @Test
    fun fraværsprosentBransjeEllerNæring_regnerUtRiktigFraværsprosentForNæring() {
        val kalkulator = Aggregeringskalkulator(
            Sykefraværsdata(mapOf(Aggregeringskategorier.Næring(Næring("00")) to synkendeSykefravær)),
            TestUtils.SISTE_PUBLISERTE_KVARTAL
        )
        val dummynæring = BransjeEllerNæring(Næring("00"))
        AssertionsForClassTypes.assertThat(kalkulator.fraværsprosentBransjeEllerNæring(dummynæring).getOrNull()?.verdi)
            .isEqualTo("5.0")
    }

    @Test
    fun fraværsprosentNorge_regnerUtRiktigFraværsprosent() {
        val kalkulator = Aggregeringskalkulator(
            Sykefraværsdata(mapOf(Aggregeringskategorier.Land to synkendeSykefravær)),
            TestUtils.SISTE_PUBLISERTE_KVARTAL
        )
        AssertionsForClassTypes.assertThat(kalkulator.fraværsprosentNorge().getOrNull()?.verdi).isEqualTo("5.0")
    }

    @Test
    fun trendBransjeEllerNæring_regnerUtRiktigTrendForNæring() {
        val kalkulator = Aggregeringskalkulator(
            Sykefraværsdata(mapOf(Aggregeringskategorier.Næring(Næring("00")) to synkendeSykefravær)),
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
