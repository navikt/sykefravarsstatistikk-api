package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import io.micrometer.core.instrument.MeterRegistry
import io.mockk.mockk
import io.mockk.verify
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringMetadataVirksomhetService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringPerStatistikkKategoriService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import org.junit.jupiter.api.Test

class ManuellEksportCronTest {
    val registry = mockk<MeterRegistry>(relaxed = true)
    val eksporteringPerStatistikkKategoriService = mockk<EksporteringPerStatistikkKategoriService>(relaxed = true)
    val eksporteringMetadataVirksomhetService = mockk<EksporteringMetadataVirksomhetService>(relaxed = true)
    val jobb = ManuellEksportCron(
        registry,
        mockk(),
        eksporteringPerStatistikkKategoriService,
        mockk(),
        eksporteringMetadataVirksomhetService
    )

    @Test
    fun `gjennomfør jobb burde importere og eksportere riktig antall kvartaler`() {
        jobb.gjennomførJobb()

        verify(exactly = 0) {
            eksporteringPerStatistikkKategoriService.eksporterPerStatistikkKategori(any(), Statistikkategori.VIRKSOMHET)
        }
        verify(exactly = 0) {
            eksporteringPerStatistikkKategoriService.eksporterPerStatistikkKategori(any(), Statistikkategori.VIRKSOMHET_GRADERT)
        }
        verify(exactly = 0) {
            eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(any())
        }

        val kvartaler = ÅrstallOgKvartal(2019, 3) tilOgMed ÅrstallOgKvartal(2023, 3)
        verify(exactly = kvartaler.size * 6) {
            eksporteringPerStatistikkKategoriService.eksporterPerStatistikkKategori(any(), any())
        }
    }
}