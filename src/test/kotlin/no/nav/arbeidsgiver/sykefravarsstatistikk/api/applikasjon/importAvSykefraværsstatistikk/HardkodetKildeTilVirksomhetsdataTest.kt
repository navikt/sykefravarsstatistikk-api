package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import io.kotest.matchers.collections.shouldBeUnique
import io.kotest.matchers.shouldBe
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Sektor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.Orgenhet
import org.junit.jupiter.api.Test

class HardkodetKildeTilVirksomhetsdataTest {
    @Test
    fun `hardkodet kilde til virksomheter burde hente ut virksomheter fra fil`() {
        val virksomheter = HardkodetKildeTilVirksomhetsdata.hentVirksomheter(ÅrstallOgKvartal(2020, 1))

        virksomheter.size shouldBe 1877
    }

    @Test
    fun `hardkodet kilde til virksomheter burde mappe verdiene til riktige felt`() {
        val virksomheter = HardkodetKildeTilVirksomhetsdata.hentVirksomheter(ÅrstallOgKvartal(2020, 1))

        virksomheter.first() shouldBe Orgenhet(
            orgnr = Orgnr("311991531"),
            navn = "Virksomhet 311991531",
            rectype = "2",
            sektor = Sektor.PRIVAT,
            næring = "42",
            næringskode = "42110",
            årstallOgKvartal = ÅrstallOgKvartal(2020, 1)
        )

        virksomheter.last() shouldBe Orgenhet(
            orgnr = Orgnr("315419786"),
            navn = "Virksomhet 315419786",
            rectype = "2",
            sektor = Sektor.PRIVAT,
            næring = "01",
            næringskode = "01479",
            årstallOgKvartal = ÅrstallOgKvartal(2020, 1)
        )
    }

    @Test
    fun `Har kun unike virksomheter`() {
        val virksomheter = HardkodetKildeTilVirksomhetsdata.hentVirksomheter(ÅrstallOgKvartal(2020, 1))

        virksomheter.map { it.orgnr }.shouldBeUnique()
    }
}