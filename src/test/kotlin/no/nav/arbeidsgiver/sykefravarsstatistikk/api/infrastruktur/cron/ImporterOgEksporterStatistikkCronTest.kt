package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.kotest.matchers.ints.exactly
import io.mockk.clearAllMocks
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
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.SykefraværsstatistikkImporteringService.ImportGjennomført
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer.PubliseringskalenderDto
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer.PubliseringsdatoerService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron.ImporterOgEksporterStatistikkCronTest.Tidspunkt.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.ImportEksportStatusRepository
import org.junit.jupiter.api.AfterEach
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
        registry = mockk(relaxed = true),
        importeringService = importeringService,
        importEksportStatusRepository = importEksportStatusRepository,
        virksomhetMetadataService = virksomhetMetadataService,
        eksporteringPerStatistikkKategoriService = eksporteringPerStatistikkKategoriService,
        eksporteringMetadataVirksomhetService = eksporteringMetadataVirksomhetService,
        publiseringsdatoerService = publiseringsdatoerService,
        clock = clock
    )

    val dummyForrigePubliseringsdato: LocalDate = LocalDate.parse("2024-02-29")
    val dummyGjeldendePeriode = ÅrstallOgKvartal(2023, 4)
    val dummyPlanlagtPubliseringsdato: LocalDate = LocalDate.parse("2024-05-30")


    @BeforeEach
    fun beforeEach() {
        every { publiseringsdatoerService.hentPubliseringsdatoer() } returns PubliseringskalenderDto(
            sistePubliseringsdato = dummyForrigePubliseringsdato,
            gjeldendePeriode = dummyGjeldendePeriode,
            nestePubliseringsdato = dummyPlanlagtPubliseringsdato,
        )
        stillKlokka(til = DagenFørPlanlagtPubliseringsdato)
    }

    @AfterEach
    fun resetMocks() {
        clearAllMocks()
    }

    @Test
    fun `skal bare legge til IMPORTERT_VIRKSOMHETDATA i lista over fullførte jobber når jobben faktisk gjennomføres`() {
        every { importEksportStatusRepository.hentFullførteJobber(any()) } returns listOf(IMPORTERT_STATISTIKK)
        every { virksomhetMetadataService.overskrivMetadataForVirksomheter(any()) } returns 1.right()

        importerOgEksporterStatistikkCron.gjennomførImportOgEksport()

        verify(exactly = 1) {
            importEksportStatusRepository.leggTilFullførtJobb(
                IMPORTERT_VIRKSOMHETDATA,
                dummyGjeldendePeriode
            )
        }
    }

    @Test
    fun `skal ikke legge til IMPORTERT_VIRKSOMHETDATA i lista over fullførte jobber når jobben ikke blir gjennomført`() {
        every { importEksportStatusRepository.hentFullførteJobber(any()) } returns listOf(IMPORTERT_STATISTIKK)
        every { virksomhetMetadataService.overskrivMetadataForVirksomheter(any()) } returns IngenRaderImportert.left()

        importerOgEksporterStatistikkCron.gjennomførImportOgEksport()
        verify(exactly = 0) { importEksportStatusRepository.leggTilFullførtJobb(IMPORTERT_VIRKSOMHETDATA, any()) }
    }


    @Test
    fun `når vi er på publiseringsdatoen skal vi hente fullførte jobber for det kommende kvartalet`() {
        stillKlokka(til = PåPlanlagtPubliseringsdato)

        importerOgEksporterStatistikkCron.gjennomførImportOgEksport()

        verify(exactly = 0) { importEksportStatusRepository.hentFullførteJobber(dummyGjeldendePeriode) }
        verify(atLeast = 1) { importEksportStatusRepository.hentFullførteJobber(dummyGjeldendePeriode.plussKvartaler(1)) }
    }

    @Test
    fun `importEksport burde markere alle jobber som kjørt når det finnes ny statistikk og ingenting feiler`() {
        stillKlokka(til = PåPlanlagtPubliseringsdato)
        every { importEksportStatusRepository.hentFullførteJobber(dummyGjeldendePeriode.plussKvartaler(1)) } returns emptyList()



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
        every { clock.instant() } returns dummyPlanlagtPubliseringsdato.minusDays(1).atStartOfDay().toInstant(UTC)

        importerOgEksporterStatistikkCron.gjennomførImportOgEksport()

        verify(exactly = 0) { importeringService.importerHvisDetFinnesNyStatistikk(any()) }
    }

    fun stillKlokka(til: Tidspunkt) {
        val nyDato = when (til) {
            is DagenFørPlanlagtPubliseringsdato -> dummyPlanlagtPubliseringsdato.minusDays(1)
            is PåPlanlagtPubliseringsdato -> dummyForrigePubliseringsdato
            is DagenEtterPlanlagtPubliseringsdato -> dummyPlanlagtPubliseringsdato.plusDays(1)
        }
        every { clock.zone } returns UTC
        every { clock.instant() } returns nyDato.atStartOfDay().toInstant(UTC)
    }


    sealed interface Tidspunkt {
        data object DagenFørPlanlagtPubliseringsdato : Tidspunkt
        data object PåPlanlagtPubliseringsdato : Tidspunkt
        data object DagenEtterPlanlagtPubliseringsdato : Tidspunkt
    }
}