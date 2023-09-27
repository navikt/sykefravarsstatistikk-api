package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class FjernStatistikkEldreEnnFemÅrSchedulerTest {
    val statiskKlokke: Clock = Clock.fixed(Instant.parse("2023-01-01T00:00:00.00Z"), ZoneId.of("Europe/Oslo"))
    val fjernStatistikkEldreEnnFemÅrScheduler = FjernStatistikkEldreEnnFemÅrScheduler(
        mock(),
        mock(),
        mock(),
        mock(),
        statiskKlokke
    )

    @Test
    fun `sjekkAtSistePubliserteKvartalErForsvarlig skal returnere feil hvis siste kvartal er utenfor siste 3 måneder`() {
        val uforsvarligKvartalIFremtiden = ÅrstallOgKvartal(2024, 1)
        val resultat =
            fjernStatistikkEldreEnnFemÅrScheduler.sjekkAtSistePubliserteKvartalErForsvarlig(uforsvarligKvartalIFremtiden)

        resultat shouldBe FjernStatistikkEldreEnnFemÅrScheduler.SistePubliserteKvartalErIkkeForsvarlig.left()
   }

    @Test
    fun `sjekkAtSistePubliserteKvartalErForsvarlig skal returnere Unit hvis siste kvartal er innenfor siste 3 måneder`() {
        val forsvarligKvartal = ÅrstallOgKvartal(2023, 1)
        val resultat =
            fjernStatistikkEldreEnnFemÅrScheduler.sjekkAtSistePubliserteKvartalErForsvarlig(forsvarligKvartal)

        resultat shouldBe Unit.right()
    }
}