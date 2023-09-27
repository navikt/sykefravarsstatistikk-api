package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.core.LockingTaskExecutor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.PubliseringsdatoerService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefravarStatistikkVirksomhetGraderingRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefravarStatistikkVirksomhetRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES

@Component
class FjernStatistikkEldreEnnFemÅrScheduler(
    private val taskExecutor: LockingTaskExecutor,
    private val publiseringsdatoerService: PubliseringsdatoerService,
    private val sykefravarStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository,
    private val sykefravarStatistikkVirksomhetGraderingRepository: SykefravarStatistikkVirksomhetGraderingRepository,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    //@Scheduled(cron = "0 0 0 0 1 ?") // månedlig kjøring
    @Scheduled(cron = "0 0 11 27 9 2023")
    fun jobb() {
        val lockAtMostFor = Duration.of(30, MINUTES)
        val lockAtLeastFor = Duration.of(1, MINUTES)
        taskExecutor.executeWithLock(
            Runnable { slettDataEldreEnnFemÅr() },
            LockConfiguration(Instant.now(), "fjern_gammel_statistikk", lockAtMostFor, lockAtLeastFor)
        )
    }

    fun slettDataEldreEnnFemÅr() {
        val sistePubliserteKvartal = publiseringsdatoerService.hentSistePubliserteKvartal()
        val femÅrIKvartaler = 4 * 5
        val femÅrSiden = sistePubliserteKvartal.minusKvartaler(femÅrIKvartaler)

        log.info("Sletter data eldre enn $femÅrSiden")

        val antallSlettetStatistikkVirksomhet = sykefravarStatistikkVirksomhetRepository.slettDataEldreEnn(femÅrSiden)
        log.info("Slettet $antallSlettetStatistikkVirksomhet rader fra sykefravar_statistikk_virksomhet")

        val antallSlettetStatistikkVirksomhetGradering = sykefravarStatistikkVirksomhetGraderingRepository.slettDataEldreEnn(femÅrSiden)
        log.info("Slettet $antallSlettetStatistikkVirksomhetGradering rader fra sykefravar_statistikk_virksomhet_med_gradering")
    }
}