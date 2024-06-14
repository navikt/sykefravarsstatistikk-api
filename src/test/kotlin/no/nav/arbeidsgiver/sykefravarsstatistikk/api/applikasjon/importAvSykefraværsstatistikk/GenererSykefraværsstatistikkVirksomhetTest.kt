package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.bigdecimal.shouldBeGreaterThanOrEquals
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.junit.jupiter.api.Test
import java.math.BigDecimal.ZERO

class GenererSykefraværsstatistikkVirksomhetTest {

    @Test
    fun `generer sykefraværsstatistikk virksomhet genererer riktig mengde data`() {
        val statistikk = SykefraværsstatistikkTestdatagenerator.genererSykefraværsstatistikkVirksomhet(
            ÅrstallOgKvartal(2021, 1)
        )

        statistikk.size shouldBe 13139
    }


    @Test
    fun `generer sykefraværsstatistikk virksomhet genererer data med riktige verdier`() {
        val statistikk = SykefraværsstatistikkTestdatagenerator.genererSykefraværsstatistikkVirksomhet(
            ÅrstallOgKvartal(2021, 1)
        )

        statistikk
            .filter { it.varighet == 'X' }
            .shouldForAll {
                it.tapteDagsverk shouldBe ZERO
            }

        statistikk
            .filter { it.varighet != 'X' }
            .shouldForAll {
                it.muligeDagsverk shouldBe ZERO
            }

        statistikk
            .groupBy { it.orgnr!! }
            .map { (_, value) ->
                value.sumOf { it.muligeDagsverk } - value.sumOf { it.tapteDagsverk }
            }.shouldForAll {
                it shouldBeGreaterThanOrEquals ZERO
            }

        statistikk.filter { it.antallPersoner == 0 }.size shouldBeGreaterThanOrEqual 210
    }

    @Test
    fun `generer sykefraværsstatistikk virksomhet genererer samme data om man kaller funksjonen flere ganger`() {
        val statistikk1 = SykefraværsstatistikkTestdatagenerator.genererSykefraværsstatistikkVirksomhet(
            ÅrstallOgKvartal(2021, 1)
        )
        val statistikk2 = SykefraværsstatistikkTestdatagenerator.genererSykefraværsstatistikkVirksomhet(
            ÅrstallOgKvartal(2021, 1)
        )

        statistikk1 shouldBe statistikk2
    }
}