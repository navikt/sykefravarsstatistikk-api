package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import arrow.core.right
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.PostImporteringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringMetadataVirksomhetService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringPerStatistikkKategoriService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class EksportAvEnkeltkvartalerCronTest {
    val eksporteringMetadataVirksomhetService = mock<EksporteringMetadataVirksomhetService>()
    val registry = mock<MeterRegistry>(defaultAnswer = { mock<Counter>()})
    val postImporteringService = mock<PostImporteringService>()
    val eksporteringPerStatistikkKategoriService = mock<EksporteringPerStatistikkKategoriService>()
    val jobb = EksportAvEnkeltkvartalerCron(
        registry,
        mock(),
        eksporteringMetadataVirksomhetService,
        eksporteringPerStatistikkKategoriService,
        postImporteringService,
    )

    @Test
    fun `gjennomfør jobb burde importere og eksportere riktig antall kvartaler`() {
        whenever(postImporteringService.overskrivMetadataForVirksomheter(any())).thenReturn(1.right())
        whenever(postImporteringService.overskrivNæringskoderForVirksomheter(any())).thenReturn(1.right())
        whenever(eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(any())).thenReturn(Unit.right())

        jobb.gjennomførJobb(ÅrstallOgKvartal(2020, 1), ÅrstallOgKvartal(2021, 4), listOf(Statistikkategori.BRANSJE))

        verify(eksporteringMetadataVirksomhetService, times(8)).eksporterMetadataVirksomhet(any())
    }
}