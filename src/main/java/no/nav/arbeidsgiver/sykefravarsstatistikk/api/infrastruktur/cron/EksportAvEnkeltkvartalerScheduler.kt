package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import arrow.core.getOrElse
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.core.LockingTaskExecutor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.PostImporteringService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering.EksporteringService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES

@Component
class EksportAvEnkeltkvartalerScheduler(
    registry: MeterRegistry,
    private val taskExecutor: LockingTaskExecutor,
    private val postImporteringService: PostImporteringService,
    private val eksporteringsService: EksporteringService,
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val noeFeilet: Counter

    private val kvartaler = listOf(ÅrstallOgKvartal(2023, 1))

    init {
        noeFeilet = registry.counter("sykefravarstatistikk_import_eller_eksport_feilet")
    }

    // TODO: Fjern scheduleringen etter at jobben har kjørt ÉN gang
    @Scheduled(fixedDelay = Long.MAX_VALUE, initialDelay = 60*1000)
    fun scheduledImporteringOgEksportering() {
        val lockAtMostFor = Duration.of(30, MINUTES)
        val lockAtLeastFor = Duration.of(1, MINUTES)
        taskExecutor.executeWithLock(
            Runnable { gjennomførImportOgEksport() },
            LockConfiguration(Instant.now(), "importering", lockAtMostFor, lockAtLeastFor)
        )
    }

    fun gjennomførImportOgEksport() {
        kvartaler.forEach { kvartal ->

            log.info("EksportAvEnkeltkvartaler har startet for $kvartal")

            postImporteringService.overskrivMetadataForVirksomheter(kvartal)
                .getOrElse {
                    noeFeilet.increment()
                    return
                }

            postImporteringService.overskrivNæringskoderForVirksomheter(kvartal)
                .getOrElse {
                    noeFeilet.increment()
                    return
                }

            postImporteringService.forberedNesteEksport(kvartal, true)
                .getOrElse {
                    noeFeilet.increment()
                    return
                }


            eksporteringsService.legacyEksporter(kvartal)
                .getOrElse {
                    noeFeilet.increment()
                    return
                }

//            eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(kvartal)
//                .getOrElse {
//                    noeFeilet.increment()
//                    return
//                }
//
//            Statistikkategori.entries.forEach { kategori ->
//                runCatching {
//                    eksporteringPerStatistikkKategoriService.eksporterPerStatistikkKategori(
//                        kvartal,
//                        kategori
//                    )
//                }.getOrElse {
//                    log.error("Eksport av kategori $kategori feilet", it)
//                    noeFeilet.increment()
//                    return
//                }
//            }

            log.info("EksportAvEnkeltkvartaler er ferdig for $kvartal")
        }
    }
}