package testUtils

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkLand
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import org.jetbrains.exposed.sql.deleteAll
import java.math.BigDecimal

object TestUtils {

    val SISTE_PUBLISERTE_KVARTAL = ÅrstallOgKvartal(2022, 1)

    fun slettAllStatistikkFraDatabase(
        sykefravarStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository? = null,
        sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository? = null,
        sykefraværStatistikkSektorRepository: SykefraværStatistikkSektorRepository? = null,
        sykefravarStatistikkVirksomhetGraderingRepository: SykefravarStatistikkVirksomhetGraderingRepository? = null,
        sykefraværStatistikkNæringRepository: SykefraværStatistikkNæringRepository? = null,
        sykefraværStatistikkNæringskodeRepository: SykefraværStatistikkNæringskodeRepository? = null,
        sykefraværStatistikkNæringskodeMedVarighetRepository: SykefraværStatistikkNæringskodeMedVarighetRepository? = null,
    ) {
        with(sykefravarStatistikkVirksomhetRepository) {
            this?.transaction { deleteAll() }
        }
        with(sykefraværStatistikkNæringRepository) {
            this?.transaction { deleteAll() }
        }

        with(sykefraværStatistikkNæringskodeMedVarighetRepository) {
            this?.transaction { deleteAll() }
        }

        with(sykefravarStatistikkVirksomhetGraderingRepository) {
            this?.transaction { deleteAll() }
        }

        with(sykefraværStatistikkNæringskodeRepository) {
            this?.transaction { deleteAll() }
        }

        with(sykefraværStatistikkLandRepository) {
            this?.transaction { deleteAll() }
        }

        with(sykefraværStatistikkSektorRepository) {
            this?.transaction { deleteAll() }
        }
    }


    fun opprettStatistikkForLand(sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository) {
        sykefraværStatistikkLandRepository.settInn(
            listOf(
                SykefraværsstatistikkLand(
                    årstall = SISTE_PUBLISERTE_KVARTAL.årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.kvartal,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal("4.0"),
                    muligeDagsverk = BigDecimal("100.0")
                ),
                SykefraværsstatistikkLand(
                    årstall = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(1).kvartal,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal("5.0"),
                    muligeDagsverk = BigDecimal("100.0")
                ),
                SykefraværsstatistikkLand(
                    årstall = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).årstall,
                    kvartal = SISTE_PUBLISERTE_KVARTAL.minusKvartaler(2).kvartal,
                    antallPersoner = 10,
                    tapteDagsverk = BigDecimal("6.0"),
                    muligeDagsverk = BigDecimal("100.0")
                ),
            )
        )
    }

    fun ImporttidspunktRepository.slettAlleImporttidspunkt() {
        transaction { deleteAll() }
    }


}
