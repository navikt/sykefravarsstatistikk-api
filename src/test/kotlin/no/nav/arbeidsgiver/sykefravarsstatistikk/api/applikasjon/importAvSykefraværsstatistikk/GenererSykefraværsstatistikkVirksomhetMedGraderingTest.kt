package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.bigdecimal.shouldBeGreaterThan
import io.kotest.matchers.bigdecimal.shouldBeGreaterThanOrEquals
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.junit.jupiter.api.Test
import java.math.BigDecimal.ZERO

class GenererSykefraværsstatistikkVirksomhetMedGraderingTest {

    @Test
    fun `genererer riktig mengde data`() {
        val statistikk = SykefraværsstatistikkTestdatagenerator.genererSykefraværsstatistikkVirksomhetMedGradering(
            ÅrstallOgKvartal(2021, 1)
        )

        statistikk.size shouldBe 1877
    }


    @Test
    fun `genererer data med riktige verdier`() {
        val statistikk = SykefraværsstatistikkTestdatagenerator.genererSykefraværsstatistikkVirksomhetMedGradering(
            ÅrstallOgKvartal(2021, 1)
        )

        statistikk.map { it.muligeDagsverk - it.tapteDagsverk }.shouldForAll {
            it shouldBeGreaterThanOrEquals ZERO
        }

        statistikk.map { it.antallSykemeldinger - it.antallGraderteSykemeldinger }.shouldForAll {
            it shouldBeGreaterThanOrEqual 0
        }

        statistikk.filter { it.antallPersoner != 0 }
            .shouldForAll { it.muligeDagsverk shouldBeGreaterThan it.antallPersoner.toBigDecimal() }


        statistikk.map { it.tapteDagsverk - it.tapteDagsverkGradertSykemelding }.shouldForAll {
            it shouldBeGreaterThanOrEquals ZERO
        }
    }


    @Test
    fun `genererer samme data om man kaller funksjonen flere ganger`() {
        val statistikk1 = SykefraværsstatistikkTestdatagenerator.genererSykefraværsstatistikkVirksomhetMedGradering(
            ÅrstallOgKvartal(2021, 1)
        )
        val statistikk2 = SykefraværsstatistikkTestdatagenerator.genererSykefraværsstatistikkVirksomhetMedGradering(
            ÅrstallOgKvartal(2021, 1)
        )

        statistikk1 shouldBe statistikk2
    }
}