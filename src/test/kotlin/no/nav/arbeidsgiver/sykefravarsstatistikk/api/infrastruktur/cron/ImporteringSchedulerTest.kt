package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import arrow.core.left
import arrow.core.right
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.VirksomhetMetadataService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.VirksomhetMetadataService.IngenRaderImportert
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron.ImportEksportJobb.IMPORTERT_STATISTIKK
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron.ImportEksportJobb.IMPORTERT_VIRKSOMHETDATA
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringMetadataVirksomhetService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringPerStatistikkKategoriService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.SykefraværsstatistikkImporteringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.ImportEksportStatusRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ImporteringSchedulerTest {
    private val importeringService = mockk<SykefraværsstatistikkImporteringService>()
    private val importEksportStatusRepository = mockk<ImportEksportStatusRepository>(relaxed = true)
    private val virksomhetMetadataService = mockk<VirksomhetMetadataService>()
    private val eksporteringsService = mockk<EksporteringService>()
    private val eksporteringPerStatistikkKategoriService = mockk<EksporteringPerStatistikkKategoriService>()
    private val eksporteringMetadataVirksomhetService = mockk<EksporteringMetadataVirksomhetService>()
    private val importeringScheduler = ImporteringScheduler(
        taskExecutor = mockk(),
        importeringService = importeringService,
        registry = mockk(relaxed = true),
        importEksportStatusRepository = importEksportStatusRepository,
        virksomhetMetadataService = virksomhetMetadataService,
        eksporteringsService = eksporteringsService,
        eksporteringPerStatistikkKategoriService = eksporteringPerStatistikkKategoriService,
        eksporteringMetadataVirksomhetService = eksporteringMetadataVirksomhetService,
    )
    val årstallOgKvartal = ÅrstallOgKvartal(2023, 3)

    @BeforeEach
    fun beforeEach() {
        // Defaults to happy case
        every { importeringService.importerHvisDetFinnesNyStatistikk() } returns årstallOgKvartal
        every { virksomhetMetadataService.overskrivMetadataForVirksomheter(any()) } returns 1.right()
        every { virksomhetMetadataService.overskrivNæringskoderForVirksomheter(any()) } returns 1.right()
        every { virksomhetMetadataService.forberedNesteEksport(any(), any()) } returns 1.right()
        every { eksporteringsService.legacyEksporter(any()) } returns 1.right()
        every { eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(any()) } returns Unit.right()
        every { eksporteringPerStatistikkKategoriService.eksporterPerStatistikkKategori(any(), any()) } returns Unit
    }

    @Test
    fun `importEksport burde ikke legge til jobb i lista over fullførte jobber når jobben ikke fullfører`() {
        every { importeringService.importerHvisDetFinnesNyStatistikk() } returns årstallOgKvartal
        every { importEksportStatusRepository.hentFullførteJobber(any()) } returns listOf(IMPORTERT_STATISTIKK)
        every { virksomhetMetadataService.overskrivMetadataForVirksomheter(any()) } returns IngenRaderImportert.left()

        importeringScheduler.gjennomførImportOgEksport()

        verify(exactly = 0) { importEksportStatusRepository.leggTilFullførtJobb(any(), any()) }
    }

    @Test
    fun `importEksport burde markere IMPORTERT_STATUISTIKK-jobb som kjørt når det finnes ny statistikk men resten feiler`() {
        every { virksomhetMetadataService.overskrivMetadataForVirksomheter(any()) } returns IngenRaderImportert.left()

        importeringScheduler.gjennomførImportOgEksport()

        verify(exactly = 1) { importEksportStatusRepository.leggTilFullførtJobb(any(), any()) }
        verify(exactly = 1) { importEksportStatusRepository.leggTilFullførtJobb(IMPORTERT_STATISTIKK, any()) }
    }

    @Test
    fun `importEksport burde markere alle jobber som kjørt når det finnes ny statistikk og ingenting feiler`() {
        importeringScheduler.gjennomførImportOgEksport()

        verify(exactly = ImportEksportJobb.entries.size) {
            importEksportStatusRepository.leggTilFullførtJobb(any(), any())
        }
    }

    @Test
    fun `importEksport bør fortsette der den slapp`() {
        every { importEksportStatusRepository.hentFullførteJobber(any()) } returns listOf(IMPORTERT_STATISTIKK, IMPORTERT_VIRKSOMHETDATA)
        importeringScheduler.gjennomførImportOgEksport()

        verify(exactly = 0) { virksomhetMetadataService.overskrivMetadataForVirksomheter(any()) }
        verify(exactly = 1) { virksomhetMetadataService.overskrivNæringskoderForVirksomheter(any())}
    }
}