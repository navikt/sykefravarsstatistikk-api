package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils

import io.vavr.control.Either
import lombok.extern.slf4j.Slf4j
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.exceptions.StatistikkException
import java.math.BigDecimal
import java.math.RoundingMode

@Slf4j
object StatistikkUtils {
    /**
     * Source of trouth for kalkulering av sykefraværsprosent basert på tapte dagsverk og mulige
     * dagsverk.
     */
    @JvmStatic
    fun kalkulerSykefraværsprosent(
        dagsverkTeller: BigDecimal?, dagsverkNevner: BigDecimal?
    ): Either<StatistikkException, BigDecimal> {
        val ANTALL_SIFRE_I_UTREGNING = 3
        val ANTALL_SIFRE_I_RESULTAT = 1

        if (dagsverkTeller == null || dagsverkNevner == null || dagsverkNevner.compareTo(BigDecimal.ZERO) == 0) {
            return Either.left(
                StatistikkException(
                    "Kan ikke regne ut prosent når antall dagsverk i nevner er lik $dagsverkNevner"
                )
            )
        }

        return Either.right(
            dagsverkTeller
                .divide(dagsverkNevner, ANTALL_SIFRE_I_UTREGNING, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100))
                .setScale(ANTALL_SIFRE_I_RESULTAT, RoundingMode.HALF_UP)
        )
    }
}

