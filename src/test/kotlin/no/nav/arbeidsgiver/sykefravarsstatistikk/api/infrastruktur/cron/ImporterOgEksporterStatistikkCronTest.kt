package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import arrow.core.left
import arrow.core.right
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringMetadataVirksomhetService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringPerStatistikkKategoriService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.VirksomhetMetadataService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.VirksomhetMetadataService.IngenRaderImportert
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.SykefraværsstatistikkImporteringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.SykefraværsstatistikkImporteringService.ImportGjennomført
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.SykefraværsstatistikkImporteringService.IngenNyData
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer.PubliseringsdatoerService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer.PubliseringskalenderDto
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron.ImportEksportJobb.EKSPORTERT_METADATA_VIRKSOMHET
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron.ImportEksportJobb.IMPORTERT_STATISTIKK
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron.ImportEksportJobb.IMPORTERT_VIRKSOMHETDATA
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron.ImporterOgEksporterStatistikkCronTest.Tidspunkt.DagenFørPlanlagtPubliseringsdato
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron.ImporterOgEksporterStatistikkCronTest.Tidspunkt.PåPlanlagtPubliseringsdato
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.ImportEksportStatusRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import org.junit.Ignore

class ImporterOgEksporterStatistikkCronTest {
    private val importeringService = mockk<SykefraværsstatistikkImporteringService>()
    private val importEksportStatusRepository = mockk<ImportEksportStatusRepository>(relaxUnitFun = true)
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

    val forrigePubliseringsdato: LocalDate = LocalDate.parse("2024-02-29")
    val gjeldendeKvartal = ÅrstallOgKvartal(2023, 4)
    val planlagtPubliseringsdato: LocalDate = LocalDate.parse("2024-05-30")

    val gjeldendeKvartalEtterPubliseringsdatoenErNådd = gjeldendeKvartal.plussKvartaler(1)

    @BeforeEach
    fun beforeEach() {
        every { publiseringsdatoerService.hentPubliseringsdatoer() } returns PubliseringskalenderDto(
            sistePubliseringsdato = forrigePubliseringsdato,
            gjeldendePeriode = gjeldendeKvartal,
            nestePubliseringsdato = planlagtPubliseringsdato,
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
                gjeldendeKvartal
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
    fun `når vi er på publiseringsdatoen skal vi hente fullførte jobber for det nye kvartalet`() {
        stillKlokka(til = PåPlanlagtPubliseringsdato)

        every { importEksportStatusRepository.hentFullførteJobber(any()) } returns emptyList()
        every { importeringService.importerHvisDetFinnesNyStatistikk(any()) } returns ImportGjennomført.right()

        importerOgEksporterStatistikkCron.gjennomførImportOgEksport()

        verify(exactly = 0) { importEksportStatusRepository.hentFullførteJobber(gjeldendeKvartal) }
        verify(atLeast = 1) {
            importEksportStatusRepository.hentFullførteJobber(
                gjeldendeKvartalEtterPubliseringsdatoenErNådd
            )
        }
    }


@Ignore
fun `importEksport skal fortsette der den slapp`() {
        every { importEksportStatusRepository.hentFullførteJobber(any()) } returns listOf(
            IMPORTERT_STATISTIKK,
            IMPORTERT_VIRKSOMHETDATA
        )
        every { eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(any()) } returns Unit.right()

        importerOgEksporterStatistikkCron.gjennomførImportOgEksport()

        verify(exactly = 0) { virksomhetMetadataService.overskrivMetadataForVirksomheter(any()) }
        verify(exactly = 1) { eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(any()) }
    }

    @Test
    fun `ingen andre jobber skjal starte opp hvis ikke vi har importert statistikk først`() {
        every { importEksportStatusRepository.hentFullførteJobber(any()) } returns emptyList()
        every { importeringService.importerHvisDetFinnesNyStatistikk(any()) } returns IngenNyData.left()

        importerOgEksporterStatistikkCron.gjennomførImportOgEksport()

        verify(exactly = 0) { virksomhetMetadataService.overskrivMetadataForVirksomheter(any()) }
        verify(exactly = 0) { eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(any()) }
        verify(exactly = 0) { eksporteringPerStatistikkKategoriService.eksporterPerStatistikkKategori(any(), any()) }
    }

    @Test
    fun `import skal ikke starte for kommende publiseringsdato hvis dagens dato er før planlagt publiseringsdato`() {
        stillKlokka(til = DagenFørPlanlagtPubliseringsdato)

        every { importEksportStatusRepository.hentFullførteJobber(gjeldendeKvartal) } returns emptyList()

        every { importeringService.importerHvisDetFinnesNyStatistikk(any()) } returns ImportGjennomført.right()

        importerOgEksporterStatistikkCron.gjennomførImportOgEksport()

        verify(exactly = 0) {
            importeringService.importerHvisDetFinnesNyStatistikk(
                gjeldendeKvartalEtterPubliseringsdatoenErNådd
            )
        }
    }

    @Test
    fun `burde markere alle jobber som kjørt når det finnes ny statistikk og ingenting feiler`() {
        stillKlokka(til = PåPlanlagtPubliseringsdato)

        every { importEksportStatusRepository.hentFullførteJobber(gjeldendeKvartalEtterPubliseringsdatoenErNådd) } returns
                emptyList() andThen
                listOf(IMPORTERT_STATISTIKK) andThen
                listOf(IMPORTERT_STATISTIKK, IMPORTERT_VIRKSOMHETDATA) andThen
                listOf(
                    IMPORTERT_STATISTIKK,
                    IMPORTERT_VIRKSOMHETDATA,
                    EKSPORTERT_METADATA_VIRKSOMHET
                )

        every { virksomhetMetadataService.overskrivMetadataForVirksomheter(gjeldendeKvartalEtterPubliseringsdatoenErNådd) } returns 1.right()
        every {
            eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(
                gjeldendeKvartalEtterPubliseringsdatoenErNådd
            )
        } returns Unit.right()
        justRun {
            eksporteringPerStatistikkKategoriService.eksporterPerStatistikkKategori(
                gjeldendeKvartalEtterPubliseringsdatoenErNådd,
                any()
            )
        }

        every {
            importeringService.importerHvisDetFinnesNyStatistikk(gjeldendeKvartalEtterPubliseringsdatoenErNådd)
        } returns ImportGjennomført.right()

        importerOgEksporterStatistikkCron.gjennomførImportOgEksport()

        verify(exactly = 4) {
            importEksportStatusRepository.leggTilFullførtJobb(any(), any())
        }
    }

    fun stillKlokka(til: Tidspunkt) {
        val nyDato = when (til) {
            is DagenFørPlanlagtPubliseringsdato -> planlagtPubliseringsdato.minusDays(1)
            is PåPlanlagtPubliseringsdato -> planlagtPubliseringsdato
        }
        every { clock.zone } returns UTC
        every { clock.instant() } returns nyDato.atStartOfDay().toInstant(UTC)
    }


    sealed interface Tidspunkt {
        data object DagenFørPlanlagtPubliseringsdato : Tidspunkt
        data object PåPlanlagtPubliseringsdato : Tidspunkt
    }
}