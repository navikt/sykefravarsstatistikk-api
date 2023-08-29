package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils

import arrow.core.Either

object EitherUtils {
    fun <L, R> getRightsAndLogLefts(vararg leftsAndRights: Either<L, R>): List<R> {
        return leftsAndRights
            .mapNotNull { it.getOrNull() }
    }
}
