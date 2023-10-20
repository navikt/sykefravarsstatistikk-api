package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhetMedGradering
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.PubliseringsdatoerRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.StatistikkRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class SykefraværsstatistikkImporteringService(
    private val statistikkRepository: StatistikkRepository,
    private val datavarehusRepository: DatavarehusRepository,
    private val publiseringsdatoerRepository: PubliseringsdatoerRepository,
    private val environment: Environment
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    fun importerHvisDetFinnesNyStatistikk(): ÅrstallOgKvartal {
        log.info("Gjeldende miljø: " + environment.activeProfiles.contentToString())
        val årstallOgKvartalForSykefraværsstatistikk = listOf(
            statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
                Statistikkilde.LAND
            ),
            statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
                Statistikkilde.SEKTOR
            ),
            statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
                Statistikkilde.NÆRING
            ),
            statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
                Statistikkilde.NÆRING_5_SIFFER
            ),
            statistikkRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
                Statistikkilde.VIRKSOMHET
            ),
        )
        val årstallOgKvartalForDvh = listOf(
            datavarehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
                StatistikkildeDvh.LAND_OG_SEKTOR
            ),
            datavarehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
                StatistikkildeDvh.NÆRING
            ),
            datavarehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
                StatistikkildeDvh.NÆRING_5_SIFFER
            ),
            datavarehusRepository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(
                StatistikkildeDvh.VIRKSOMHET
            ),
        )
        val gjeldendeÅrstallOgKvartal = årstallOgKvartalForDvh[0]
        return if (kanImportStartes(årstallOgKvartalForSykefraværsstatistikk, årstallOgKvartalForDvh)) {
            log.info("Importerer ny statistikk")
            importerNyStatistikk(gjeldendeÅrstallOgKvartal)
            oppdaterPubliseringsstatus(gjeldendeÅrstallOgKvartal)
            gjeldendeÅrstallOgKvartal
        } else {
            log.info("Importerer ikke statistikk")
            gjeldendeÅrstallOgKvartal
        }
    }

    private fun oppdaterPubliseringsstatus(gjeldendeÅrstallOgKvartal: ÅrstallOgKvartal) {
        publiseringsdatoerRepository.oppdaterSisteImporttidspunkt(gjeldendeÅrstallOgKvartal)
    }


    fun kanImportStartes(
        årstallOgKvartalForSfsDb: List<ÅrstallOgKvartal>,
        årstallOgKvartalForDvh: List<ÅrstallOgKvartal>
    ): Boolean {
        val allImportertStatistikkHarSammeÅrstallOgKvartal = alleErLike(årstallOgKvartalForSfsDb)
        val allStatistikkFraDvhHarSammeÅrstallOgKvartal = alleErLike(årstallOgKvartalForDvh)
        if (!allImportertStatistikkHarSammeÅrstallOgKvartal
            || !allStatistikkFraDvhHarSammeÅrstallOgKvartal
        ) {
            log.warn(
                "Kunne ikke importere ny statistikk, tabellene hadde forskjellige årstall og kvartal. "
                        + "Kvartaler Sykefraværsstatistikk-DB: {}. Kvartaler DVH: {}",
                årstallOgKvartalForSfsDb,
                årstallOgKvartalForDvh
            )
            return false
        }
        val sisteÅrstallOgKvartalForDvh = årstallOgKvartalForDvh[0]
        val sisteÅrstallOgKvartalForSykefraværsstatistikk = årstallOgKvartalForSfsDb[0]
        val importertStatistikkLiggerEttKvartalBakDvh = (sisteÅrstallOgKvartalForDvh
            .minusKvartaler(1)
                == sisteÅrstallOgKvartalForSykefraværsstatistikk)
        return if (importertStatistikkLiggerEttKvartalBakDvh) {
            log.info(
                "Skal importere statistikk fra Dvh for årstall {} og kvartal {}",
                sisteÅrstallOgKvartalForDvh.årstall,
                sisteÅrstallOgKvartalForDvh.kvartal
            )
            true
        } else if (sisteÅrstallOgKvartalForDvh == sisteÅrstallOgKvartalForSykefraværsstatistikk) {
            log.info(
                "Skal ikke importere statistikk fra Dvh for årstall {} og kvartal {}. Ingen "
                        + "ny statistikk funnet.",
                sisteÅrstallOgKvartalForDvh.årstall,
                sisteÅrstallOgKvartalForDvh.kvartal
            )
            false
        } else {
            log.warn(
                "Kunne ikke importere ny statistikk fra Dvh fordi årstall {} og kvartal {} ikke ligger "
                        + "nøyaktig ett kvartal foran vår statistikk som har årstall {} og kvartal {}.",
                sisteÅrstallOgKvartalForDvh.årstall,
                sisteÅrstallOgKvartalForDvh.kvartal,
                sisteÅrstallOgKvartalForSykefraværsstatistikk.årstall,
                sisteÅrstallOgKvartalForSykefraværsstatistikk.kvartal
            )
            false
        }
    }

    private fun importerNyStatistikk(årstallOgKvartal: ÅrstallOgKvartal) {
        importSykefraværsstatistikkLand(årstallOgKvartal)
        importSykefraværsstatistikkSektor(årstallOgKvartal)
        importSykefraværsstatistikkNæring(årstallOgKvartal)
        importSykefraværsstatistikkNæring5siffer(årstallOgKvartal)
        importSykefraværsstatistikkNæringMedVarighet(årstallOgKvartal)
        importSykefraværsstatistikkVirksomhet(årstallOgKvartal)
        importSykefraværsstatistikkVirksomhetMedGradering(årstallOgKvartal)
    }

    private fun importSykefraværsstatistikkLand(
        årstallOgKvartal: ÅrstallOgKvartal
    ): SlettOgOpprettResultat {
        val sykefraværsstatistikkLand = datavarehusRepository.hentSykefraværsstatistikkLand(årstallOgKvartal)
        val resultat = statistikkRepository.importSykefraværsstatistikkLand(
            sykefraværsstatistikkLand, årstallOgKvartal
        )
        loggResultat(årstallOgKvartal, resultat, "land")
        return resultat
    }

    private fun importSykefraværsstatistikkSektor(
        årstallOgKvartal: ÅrstallOgKvartal
    ): SlettOgOpprettResultat {
        val sykefraværsstatistikkSektor = datavarehusRepository.hentSykefraværsstatistikkSektor(årstallOgKvartal)
        val resultat = statistikkRepository.importSykefraværsstatistikkSektor(
            sykefraværsstatistikkSektor, årstallOgKvartal
        )
        loggResultat(årstallOgKvartal, resultat, "sektor")
        return resultat
    }

    private fun importSykefraværsstatistikkNæring(
        årstallOgKvartal: ÅrstallOgKvartal
    ): SlettOgOpprettResultat {
        val sykefraværsstatistikkNæring = datavarehusRepository.hentSykefraværsstatistikkNæring(årstallOgKvartal)
        val resultat = statistikkRepository.importSykefraværsstatistikkNæring(
            sykefraværsstatistikkNæring, årstallOgKvartal
        )
        loggResultat(årstallOgKvartal, resultat, "næring")
        return resultat
    }

    private fun importSykefraværsstatistikkNæring5siffer(
        årstallOgKvartal: ÅrstallOgKvartal
    ): SlettOgOpprettResultat {
        val sykefraværsstatistikkNæring = datavarehusRepository.hentSykefraværsstatistikkNæring5siffer(årstallOgKvartal)
        val resultat = statistikkRepository.importSykefraværsstatistikkNæring5siffer(
            sykefraværsstatistikkNæring, årstallOgKvartal
        )
        loggResultat(årstallOgKvartal, resultat, "næring5siffer")
        return resultat
    }

    private fun importSykefraværsstatistikkVirksomhet(årstallOgKvartal: ÅrstallOgKvartal) {
        val statistikk: List<SykefraværsstatistikkVirksomhet> = if (currentEnvironmentIsProd()) {
            datavarehusRepository.hentSykefraværsstatistikkVirksomhet(årstallOgKvartal)
        } else {
            SykefraværsstatistikkImporteringUtils.genererSykefraværsstatistikkVirksomhet(årstallOgKvartal)
        }
        val resultat = statistikkRepository.importSykefraværsstatistikkVirksomhet(statistikk, årstallOgKvartal)
        loggResultat(årstallOgKvartal, resultat, "virksomhet")
    }

    private fun importSykefraværsstatistikkVirksomhetMedGradering(
        årstallOgKvartal: ÅrstallOgKvartal
    ) {
        val statistikk: List<SykefraværsstatistikkVirksomhetMedGradering> = if (currentEnvironmentIsProd()) {
            datavarehusRepository.hentSykefraværsstatistikkVirksomhetMedGradering(årstallOgKvartal)
        } else {
            SykefraværsstatistikkImporteringUtils.genererSykefraværsstatistikkVirksomhetMedGradering(årstallOgKvartal)
        }
        val resultat = statistikkRepository.importSykefraværsstatistikkVirksomhetMedGradering(
            statistikk, årstallOgKvartal
        )
        loggResultat(årstallOgKvartal, resultat, "virksomhet gradert sykemelding")
    }

    private fun importSykefraværsstatistikkNæringMedVarighet(
        årstallOgKvartal: ÅrstallOgKvartal
    ): SlettOgOpprettResultat {
        val sykefraværsstatistikkNæringMedVarighet =
            datavarehusRepository.hentSykefraværsstatistikkNæringMedVarighet(årstallOgKvartal)
        val resultat = statistikkRepository.importSykefraværsstatistikkNæringMedVarighet(
            sykefraværsstatistikkNæringMedVarighet, årstallOgKvartal
        )
        loggResultat(årstallOgKvartal, resultat, "næring med varighet")
        return resultat
    }

    private fun loggResultat(
        årstallOgKvartal: ÅrstallOgKvartal, resultat: SlettOgOpprettResultat, type: String
    ) {
        val melding: String = if (resultat.antallRadOpprettet == 0 && resultat.antallRadSlettet == 0) {
            "Ingenting har blitt slettet eller importert."
        } else {
            String.format(
                "Antall rader opprettet: %d, antall slettet: %d",
                resultat.antallRadOpprettet, resultat.antallRadSlettet
            )
        }
        log.info(
            "Import av sykefraværsstatistikk av type "
                    + type
                    + " for "
                    + årstallOgKvartal
                    + " i miljø "
                    + environment
                    + " er ferdig: "
                    + melding
        )
    }

    private fun alleErLike(årstallOgKvartal: List<ÅrstallOgKvartal>): Boolean {
        val førsteÅrstallOgKvartal = årstallOgKvartal[0]
        return årstallOgKvartal.stream().allMatch { p: ÅrstallOgKvartal -> p == førsteÅrstallOgKvartal }
    }

    private fun currentEnvironmentIsProd(): Boolean {
        return environment.activeProfiles.contains("prod")
    }
}
