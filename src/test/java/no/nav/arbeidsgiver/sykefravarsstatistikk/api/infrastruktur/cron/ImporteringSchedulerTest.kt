package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import arrow.core.left
import arrow.core.right
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.PostImporteringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.PostImporteringService.IngenRaderImportert
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ImportEksportJobb.IMPORTERT_STATISTIKK
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringMetadataVirksomhetService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringPerStatistikkKategoriService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering.SykefraværsstatistikkImporteringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering.SykefraværsstatistikkImporteringService.KunneIkkeImportere
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.ImportEksportStatusRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ImporteringSchedulerTest {
    val importeringService = mockk<SykefraværsstatistikkImporteringService>()
    val importEksportStatusRepository = mockk<ImportEksportStatusRepository>(relaxed = true)
    val postImporteringService = mockk<PostImporteringService>()
    val eksporteringsService = mockk<EksporteringService>()
    val eksporteringPerStatistikkKategoriService = mockk<EksporteringPerStatistikkKategoriService>()
    val eksporteringMetadataVirksomhetService = mockk<EksporteringMetadataVirksomhetService>()
    private val importeringScheduler = ImporteringScheduler(
        taskExecutor = mockk(),
        importeringService = importeringService,
        registry = mockk(relaxed = true),
        importEksportStatusRepository = importEksportStatusRepository,
        postImporteringService = postImporteringService,
        eksporteringsService = eksporteringsService,
        eksporteringPerStatistikkKategoriService = eksporteringPerStatistikkKategoriService,
        eksporteringMetadataVirksomhetService = eksporteringMetadataVirksomhetService,
    )
    val årstallOgKvartal = ÅrstallOgKvartal(2023, 3)

    @BeforeEach
    fun beforeEach() {
        every { importeringService.importerHvisDetFinnesNyStatistikk() } returns årstallOgKvartal.right()
        every { postImporteringService.overskrivMetadataForVirksomheter(any()) } returns 1.right()
        every { postImporteringService.overskrivNæringskoderForVirksomheter(any()) } returns 1.right()
        every { postImporteringService.forberedNesteEksport(any(), any()) } returns 1 // TODO: Legg til feilhåndtering
        every { eksporteringsService.legacyEksporter(any()) } returns 1 // TODO: Legg til feilhåndtering?
        every { eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(any()) } returns Unit // TODO: Legg til feilhåndtering
        every { eksporteringPerStatistikkKategoriService.eksporterPerStatistikkKategori(any(), any()) } // TODO: Legg til feilhåndtering
    }

    @Test
    fun `importering burde ikke markere jobb som kjørt når det ikke finnes ny statistikk`() {
        every { importeringService.importerHvisDetFinnesNyStatistikk() } returns KunneIkkeImportere.left()

        importeringScheduler.importering()

        verify { importEksportStatusRepository wasNot Called }
    }

    @Test
    fun `importering burde markere importert statistikk jobb som kjørt når det finnes ny statistikk men resten feiler`() {
        every { postImporteringService.overskrivMetadataForVirksomheter(any()) } returns IngenRaderImportert.left()

        importeringScheduler.importering()

        verify(exactly = 1) { importEksportStatusRepository.markerJobbSomKjørt(any(), any()) }
        verify(exactly = 1) { importEksportStatusRepository.markerJobbSomKjørt(any(), IMPORTERT_STATISTIKK) }
    }

     @Test
     fun `importering burde markere alle jobber som kjørt når det finnes ny statistikk og ingenting feiler`() {
         importeringScheduler.importering()

         verify(exactly = 6 + Statistikkategori.entries.size) {
             importEksportStatusRepository.markerJobbSomKjørt(any(), any())
         }
     }
}