package no.nav.arbeidsgiver.sykefravarsstatistikk.api

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.SykefravarStatistikkVirksomhetRepository
import org.springframework.context.support.beans

val prodBeans = beans {
    // Services
//    bean<KvartalsvisSykefraværshistorikkService>()

    // Controllers

    // Repositories
    bean<SykefravarStatistikkVirksomhetRepository>()
//    bean<SykefraværStatistikkLandRepository>()
//    bean<SykefraværStatistikkSektorRepository>()
//    bean<SykefraværStatistikkNæringRepository>()
//    bean<SykefraværStatistikkNæringskodeRepository>()
}