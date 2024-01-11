package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import arrow.core.right
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringMetadataVirksomhetService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.VirksomhetMetadataService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class ImportOgEksportAvEnkeltkvartalerCronTest {
    val eksporteringMetadataVirksomhetService = mock<EksporteringMetadataVirksomhetService>()
    val registry = mock<MeterRegistry>(defaultAnswer = { mock<Counter>()})
    val virksomhetMetadataService = mock<VirksomhetMetadataService>()
    val jobb = ImportOgEksportAvEnkeltkvartalerCron(
        registry,
        mock(),
        mock(),
        virksomhetMetadataService,
        eksporteringMetadataVirksomhetService
    )

    @Test
    fun `gjennomfør jobb burde importere og eksportere riktig antall kvartaler`() {
        whenever(virksomhetMetadataService.overskrivMetadataForVirksomheter(any())).thenReturn(1.right())
        whenever(eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(any())).thenReturn(Unit.right())

        val kvartaler =  ÅrstallOgKvartal(2021, 4) inkludertTidligere 7
        jobb.gjennomførJobb(kvartaler, listOf(Statistikkategori.BRANSJE))

        verify(eksporteringMetadataVirksomhetService, times(8)).eksporterMetadataVirksomhet(any())
    }
}