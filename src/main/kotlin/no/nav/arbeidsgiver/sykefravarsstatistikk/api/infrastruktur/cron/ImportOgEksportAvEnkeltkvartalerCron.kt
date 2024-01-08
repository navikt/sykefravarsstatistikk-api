package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import arrow.core.getOrElse
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.core.LockingTaskExecutor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringMetadataVirksomhetService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.EksporteringPerStatistikkKategoriService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.VirksomhetMetadataService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Statistikkategori
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.SykefraværsstatistikkImporteringService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES

@Component
class ImportOgEksportAvEnkeltkvartalerCron(
    registry: MeterRegistry,
    private val taskExecutor: LockingTaskExecutor,
    private val eksporteringMetadataVirksomhetService: EksporteringMetadataVirksomhetService,
    private val eksporteringPerStatistikkKategoriService: EksporteringPerStatistikkKategoriService,
    private val virksomhetMetadataService: VirksomhetMetadataService,
    private val sykefraværsstatistikkImporteringService: SykefraværsstatistikkImporteringService,
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val noeFeiletCounter: Counter = registry.counter("sykefravarstatistikk_import_eller_eksport_feilet")

    @Scheduled(cron = "0 0 13 8 1 ?")
    fun scheduledImportOgEksportAvEnkeltkvartaler() {
        val kategorier = listOf(Statistikkategori.VIRKSOMHET, Statistikkategori.VIRKSOMHET_GRADERT)
//        val sisteFireÅr = ÅrstallOgKvartal(2023, 3) inkludertTidligere 4 * 4 - 1
        val sisteKvartal = listOf(ÅrstallOgKvartal(2023, 3))

        val lockAtMostFor = Duration.of(30, MINUTES)
        val lockAtLeastFor = Duration.of(1, MINUTES)
        taskExecutor.executeWithLock(
            Runnable { gjennomførJobb(sisteKvartal.sorted(), kategorier) },
            LockConfiguration(Instant.now(), "importering", lockAtMostFor, lockAtLeastFor)
        )
    }

    fun gjennomførJobb(kvartaler: List<ÅrstallOgKvartal>, kategorier: List<Statistikkategori>) {
        log.info("Starter EksportAvEnkeltkvartaler for $kvartaler")
        for (kvartal in kvartaler) {
//            log.info("Importerer statistikk for $kvartal...")
//            sykefraværsstatistikkImporteringService.importerAlleKategorier(kvartal)
//            log.info("import av $kvartal er fullført")
//
            log.info("Overskriver metadata for $kvartal...")
            virksomhetMetadataService.overskrivMetadataForVirksomheter(kvartal)
                .getOrElse {
                    noeFeiletCounter.increment()
                    return
                }

            virksomhetMetadataService.overskrivNæringskoderForVirksomheter(kvartal)
                .getOrElse {
                    noeFeiletCounter.increment()
                    return
                }
            log.info("Overskriving av metadata for $kvartal er fullført")

            log.info("Eksporterer metadata for $kvartal...")
            eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(kvartal)
                .getOrElse {
                    noeFeiletCounter.increment()
                    return
                }
            log.info("Eksportering av metadata for $kvartal er fullført")

//            kategorier.forEach { kategori ->
//                log.info("Eksporterer statistikk ($kategori) for $kvartal...")
//                runCatching {
//                    eksporteringPerStatistikkKategoriService.eksporterPerStatistikkKategori(
//                        kvartal,
//                        kategori
//                    )
//                }.getOrElse {
//                    log.error("Eksport av kategori $kategori feilet", it)
//                    noeFeiletCounter.increment()
//                    return
//                }
//                log.info("Eksportering av statistikk ($kategori) for $kvartal er ferdig")
//            }

            log.info("EksportAvEnkeltkvartaler er ferdig for $kvartal")
        }
//        log.info("EksportAvEnkeltkvartaler er ferdig for $kvartaler")
    }
}
