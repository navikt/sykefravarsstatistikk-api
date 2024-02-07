package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import arrow.core.left
import arrow.core.right
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringMetadataVirksomhetService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringPerStatistikkKategoriService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.VirksomhetMetadataService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.junit.jupiter.api.Test

class ManuellEksportCronTest {
    val registry = mockk<MeterRegistry>(relaxed = true)
    val eksporteringPerStatistikkKategoriService = mockk<EksporteringPerStatistikkKategoriService>(relaxed = true)
    val eksporteringMetadataVirksomhetService = mockk<EksporteringMetadataVirksomhetService>(relaxed = true)
    val virksomhetMetadataService = mockk<VirksomhetMetadataService>(relaxed = true)
    val jobb = ManuellEksportCron(
        registry,
        mockk(),
        eksporteringPerStatistikkKategoriService,
        virksomhetMetadataService,
        eksporteringMetadataVirksomhetService
    )

    @Test
    fun `gjennomfør jobb burde importere og eksportere riktig antall kvartaler`() {
        every { virksomhetMetadataService.overskrivMetadataForVirksomheter(any()) } returns 1.right()
        every { eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(any()) } returns Unit.right()

        jobb.gjennomførJobb()

        verify(exactly = 0) {
            eksporteringPerStatistikkKategoriService.eksporterPerStatistikkKategori(any(), any())
        }

        val antallKvartaler = 1
        val antallKategorier = Statistikkategori.entries.size
        val antallEksporteringer = antallKvartaler * antallKategorier

        verify(exactly = antallEksporteringer) {
            eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(any())
        }
    }
}