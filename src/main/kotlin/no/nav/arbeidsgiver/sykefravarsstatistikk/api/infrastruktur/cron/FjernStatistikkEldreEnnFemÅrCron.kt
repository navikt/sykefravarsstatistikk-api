package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.cron

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.core.LockingTaskExecutor
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.PubliseringsdatoerService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.felles.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefravarStatistikkVirksomhetGraderingRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefravarStatistikkVirksomhetRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit.MINUTES

@Component
open class FjernStatistikkEldreEnnFemÅrCron(
    private val taskExecutor: LockingTaskExecutor,
    private val publiseringsdatoerService: PubliseringsdatoerService,
    private val sykefravarStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository,
    private val sykefravarStatistikkVirksomhetGraderingRepository: SykefravarStatistikkVirksomhetGraderingRepository,
    private val clock: Clock,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Scheduled(cron = "0 0 0 1 * ?") // månedlig kjøring
    fun jobb() {
        val lockAtMostFor = Duration.of(30, MINUTES)
        val lockAtLeastFor = Duration.of(1, MINUTES)
        taskExecutor.executeWithLock(
            Runnable { slettDataEldreEnnFemÅr() },
            LockConfiguration(Instant.now(clock), "fjern_gammel_statistikk", lockAtMostFor, lockAtLeastFor)
        )
    }

    fun slettDataEldreEnnFemÅr() {
        val sistePubliserteKvartal = publiseringsdatoerService.hentSistePubliserteKvartal()

        sjekkAtSistePubliserteKvartalErForsvarlig(sistePubliserteKvartal).getOrElse {
            log.error("Siste publiserte kvartal $sistePubliserteKvartal er ikke forsvarlig")
            return
        }

        val femÅrIKvartaler = 4 * 5
        val femÅrSiden = sistePubliserteKvartal.minusKvartaler(femÅrIKvartaler)

        log.info("Sletter data eldre enn $femÅrSiden")

        val antallSlettetStatistikkVirksomhet = sykefravarStatistikkVirksomhetRepository.slettDataEldreEnn(femÅrSiden)
        log.info("Slettet $antallSlettetStatistikkVirksomhet rader fra sykefravar_statistikk_virksomhet")

        val antallSlettetStatistikkVirksomhetGradering =
            sykefravarStatistikkVirksomhetGraderingRepository.slettDataEldreEnn(femÅrSiden)
        log.info("Slettet $antallSlettetStatistikkVirksomhetGradering rader fra sykefravar_statistikk_virksomhet_med_gradering")
    }

    open fun sjekkAtSistePubliserteKvartalErForsvarlig(årstallOgKvartal: ÅrstallOgKvartal): Either<SistePubliserteKvartalErIkkeForsvarlig, Unit> {
        val nå = LocalDate.now(clock)
        val treMånederSiden = nå.minusMonths(6)
        val startAvKvartalet = LocalDate.of(årstallOgKvartal.årstall, (årstallOgKvartal.kvartal * 3) - 2, 1)
        if (startAvKvartalet in treMånederSiden..nå) {
            return Unit.right()
        }
        return SistePubliserteKvartalErIkkeForsvarlig.left()
    }

    object SistePubliserteKvartalErIkkeForsvarlig
}