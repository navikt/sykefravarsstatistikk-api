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
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.SykefraværsstatistikkImporteringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer.Publiseringsdatoer
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer.PubliseringsdatoerService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.ImportEksportStatusRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset.UTC

class ImporterOgEksporterStatistikkCronTest {
    private val importeringService = mockk<SykefraværsstatistikkImporteringService>()
    private val importEksportStatusRepository = mockk<ImportEksportStatusRepository>(relaxed = true)
    private val virksomhetMetadataService = mockk<VirksomhetMetadataService>()
    private val eksporteringPerStatistikkKategoriService = mockk<EksporteringPerStatistikkKategoriService>()
    private val eksporteringMetadataVirksomhetService = mockk<EksporteringMetadataVirksomhetService>()
    private val publiseringsdatoerService = mockk<PubliseringsdatoerService>()
    private val clock = mockk<Clock>()
    private val importerOgEksporterStatistikkCron = ImporterOgEksporterStatistikkCron(
        taskExecutor = mockk(),
        importeringService = importeringService,
        registry = mockk(relaxed = true),
        importEksportStatusRepository = importEksportStatusRepository,
        virksomhetMetadataService = virksomhetMetadataService,
        eksporteringPerStatistikkKategoriService = eksporteringPerStatistikkKategoriService,
        eksporteringMetadataVirksomhetService = eksporteringMetadataVirksomhetService,
        publiseringsdatoerService = publiseringsdatoerService,
        clock = clock
    )

    val nestePubliseringsdato: LocalDate = LocalDate.of(2023, 12, 24)

    @BeforeEach
    fun beforeEach() {
        // Defaults to happy case
        every { importeringService.importerHvisDetFinnesNyStatistikk() } returns Unit
        every { virksomhetMetadataService.overskrivMetadataForVirksomheter(any()) } returns 1.right()
        every { eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(any()) } returns Unit.right()
        every { eksporteringPerStatistikkKategoriService.eksporterPerStatistikkKategori(any(), any()) } returns Unit
        every { publiseringsdatoerService.hentPubliseringsdatoer() } returns
                Publiseringsdatoer(
                    sistePubliseringsdato = LocalDate.of(2023, 1, 1),
                    gjeldendePeriode = ÅrstallOgKvartal(2023, 3),
                    nestePubliseringsdato = nestePubliseringsdato
                )
        every { clock.instant() } returns nestePubliseringsdato.atStartOfDay().toInstant(UTC)
        every { clock.zone } returns UTC
    }

    @Test
    fun `importEksport burde ikke legge til jobb i lista over fullførte jobber når jobben ikke fullfører`() {
        every { importEksportStatusRepository.hentFullførteJobber(any()) } returns listOf(IMPORTERT_STATISTIKK)
        every { virksomhetMetadataService.overskrivMetadataForVirksomheter(any()) } returns IngenRaderImportert.left()

        importerOgEksporterStatistikkCron.gjennomførImportOgEksport()

        verify(exactly = 0) { importEksportStatusRepository.leggTilFullførtJobb(any(), any()) }
    }

    @Test
    fun `importEksport burde markere IMPORTERT_STATUISTIKK-jobb som kjørt når det finnes ny statistikk men resten feiler`() {
        every { virksomhetMetadataService.overskrivMetadataForVirksomheter(any()) } returns IngenRaderImportert.left()

        importerOgEksporterStatistikkCron.gjennomførImportOgEksport()

        verify(exactly = 1) { importEksportStatusRepository.leggTilFullførtJobb(any(), any()) }
        verify(exactly = 1) { importEksportStatusRepository.leggTilFullførtJobb(IMPORTERT_STATISTIKK, any()) }
    }

    @Test
    fun `importEksport burde markere alle jobber som kjørt når det finnes ny statistikk og ingenting feiler`() {
        importerOgEksporterStatistikkCron.gjennomførImportOgEksport()

        verify(exactly = ImportEksportJobb.entries.size) {
            importEksportStatusRepository.leggTilFullførtJobb(any(), any())
        }
    }

    @Test
    fun `importEksport bør fortsette der den slapp`() {
        every { importEksportStatusRepository.hentFullførteJobber(any()) } returns listOf(
            IMPORTERT_STATISTIKK,
            IMPORTERT_VIRKSOMHETDATA
        )
        importerOgEksporterStatistikkCron.gjennomførImportOgEksport()

        verify(exactly = 0) { virksomhetMetadataService.overskrivMetadataForVirksomheter(any()) }
    }

    @Test
    fun `import skal ikke starte hvis dagens dato er før planlagt publiseringsdato`() {
        every { clock.instant() } returns nestePubliseringsdato.minusDays(1).atStartOfDay().toInstant(UTC)

        importerOgEksporterStatistikkCron.gjennomførImportOgEksport()

        verify(exactly = 0) { importeringService.importerHvisDetFinnesNyStatistikk() }
    }
}