package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import arrow.core.right
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.VirksomhetMetadataService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringMetadataVirksomhetService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringPerStatistikkKategoriService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class EksportAvEnkeltkvartalerCronTest {
    val eksporteringMetadataVirksomhetService = mock<EksporteringMetadataVirksomhetService>()
    val registry = mock<MeterRegistry>(defaultAnswer = { mock<Counter>()})
    val virksomhetMetadataService = mock<VirksomhetMetadataService>()
    val eksporteringPerStatistikkKategoriService = mock<EksporteringPerStatistikkKategoriService>()
    val jobb = EksportAvEnkeltkvartalerCron(
        registry,
        mock(),
        eksporteringMetadataVirksomhetService,
        eksporteringPerStatistikkKategoriService,
        virksomhetMetadataService,
    )

    @Test
    fun `gjennomfør jobb burde importere og eksportere riktig antall kvartaler`() {
        whenever(virksomhetMetadataService.overskrivMetadataForVirksomheter(any())).thenReturn(1.right())
        whenever(virksomhetMetadataService.overskrivNæringskoderForVirksomheter(any())).thenReturn(1.right())
        whenever(eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(any())).thenReturn(Unit.right())

        jobb.gjennomførJobb(ÅrstallOgKvartal(2020, 1), ÅrstallOgKvartal(2021, 4), listOf(Statistikkategori.BRANSJE))

        verify(eksporteringMetadataVirksomhetService, times(8)).eksporterMetadataVirksomhet(any())
    }
}