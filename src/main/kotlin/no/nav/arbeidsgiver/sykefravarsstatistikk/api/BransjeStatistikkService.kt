package no.nav.arbeidsgiver.sykefravarsstatistikk.api

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværStatistikkNæringRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefraværStatistikkNæringskodeRepository
import org.springframework.stereotype.Component

@Component
class BransjeStatistikkService(
    private val sykefraværStatistikkNæringRepository: SykefraværStatistikkNæringRepository,
    private val sykefraværStatistikkNæringskodeRepository: SykefraværStatistikkNæringskodeRepository,
) {

}