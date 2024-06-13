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
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES

@Component
class ManuellEksportCron(
    registry: MeterRegistry,
    private val taskExecutor: LockingTaskExecutor,
    private val eksporteringPerStatistikkKategoriService: EksporteringPerStatistikkKategoriService,
    private val virksomhetMetadataService: VirksomhetMetadataService,
    private val eksporteringMetadataVirksomhetService: EksporteringMetadataVirksomhetService,
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val noeFeiletCounter: Counter = registry.counter("sykefravarstatistikk_import_eller_eksport_feilet")

    private val kvartalSomSkalEksporteres = ÅrstallOgKvartal(2024, 1)

    //@Scheduled(cron = "0 35 15 11 06 ?")
    fun scheduledManuellEksportCron() {
        val lockAtMostFor = Duration.of(30, MINUTES)
        val lockAtLeastFor = Duration.of(1, MINUTES)
        taskExecutor.executeWithLock(
            Runnable { gjennomførJobb() },
            LockConfiguration(Instant.now(), "importering", lockAtMostFor, lockAtLeastFor)
        )
    }

    fun gjennomførJobb() {
        val kategorier = Statistikkategori.entries
        val kvartaler = listOf(kvartalSomSkalEksporteres)
        log.info("Starter EksportAvEnkeltkvartaler for $kvartaler")

        for (kvartal in kvartaler) {
            log.info("Overskriver metadata for $kvartal...")

            virksomhetMetadataService.overskrivMetadataForVirksomheter(kvartal)
                .getOrElse {
                    noeFeiletCounter.increment()
                    return
                }


            log.info("Eksporterer metadata for $kvartal...")
            eksporteringMetadataVirksomhetService.eksporterMetadataVirksomhet(kvartal)
                .getOrElse {
                    noeFeiletCounter.increment()
                    return
                }
            log.info("Eksportering av metadata for $kvartal er fullført")

            kategorier.forEach { kategori ->
                log.info("Eksporterer statistikk ($kategori) for $kvartal...")
                runCatching {
                    eksporteringPerStatistikkKategoriService.eksporterPerStatistikkKategori(
                        kvartal,
                        kategori
                    )
                }.getOrElse {
                    log.error("Eksport av kategori $kategori feilet", it)
                    noeFeiletCounter.increment()
                    return
                }
                log.info("Eksportering av statistikk ($kategori) for $kvartal er ferdig")
            }

            log.info("Manuell eksport er ferdig for $kvartal")
        }
        log.info("Manuell eksport er ferdig for $kvartaler")
    }
}
