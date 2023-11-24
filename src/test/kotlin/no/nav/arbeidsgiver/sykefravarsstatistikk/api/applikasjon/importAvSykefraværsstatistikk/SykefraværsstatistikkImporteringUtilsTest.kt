package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.junit.jupiter.api.Test

class SykefraværsstatistikkImporteringUtilsTest {
    @Test
    fun `generer sykefraværsstatistikk virksomhet genererer riktig mengde data`() {
        val statistikk = SykefraværsstatistikkImporteringUtils.genererSykefraværsstatistikkVirksomhet(
            ÅrstallOgKvartal(2021, 1)
        )

        statistikk.size shouldBe 2233
    }

    @Test
    fun `generer sykefraværsstatistikk virksomhet genererer data med riktige verdier`() {
        TODO()
    }
}