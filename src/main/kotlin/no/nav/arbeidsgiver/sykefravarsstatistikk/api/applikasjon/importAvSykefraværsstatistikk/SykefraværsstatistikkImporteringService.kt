package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhetMedGradering
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.SlettOgOpprettResultat
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.StatistikkildeDvh
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusLandRespository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class SykefraværsstatistikkImporteringService(
    private val sykefraværStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository,
    private val datavarehusRepository: DatavarehusRepository,
    private val importtidspunktRepository: ImporttidspunktRepository,
    private val sykefraværsstatistikkLandRepository: SykefraværStatistikkLandRepository,
    private val sykefraværStatistikkVirksomhetGraderingRepository: SykefravarStatistikkVirksomhetGraderingRepository,
    private val sykefraværStatistikkNæringskodeMedVarighetRepository: SykefraværStatistikkNæringskodeMedVarighetRepository,
    private val sykefraværStatistikkSektorRepository: SykefraværStatistikkSektorRepository,
    private val sykefraværStatistikkNæringRepository: SykefraværStatistikkNæringRepository,
    private val sykefraværStatistikkNæringskodeRepository: SykefraværStatistikkNæringskodeRepository,
    private val datavarehusLandRespository: DatavarehusLandRespository,

    private val environment: Environment,
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    fun importerHvisDetFinnesNyStatistikk(): ÅrstallOgKvartal {
        log.info("Gjeldende miljø: " + environment.activeProfiles.contentToString())
        val årstallOgKvartalForSykefraværsstatistikk = listOf(
            sykefraværsstatistikkLandRepository.hentNyesteKvartal(),
            sykefraværStatistikkSektorRepository.hentNyesteKvartal(),
            sykefraværStatistikkNæringRepository.hentNyesteKvartal(),
            sykefraværStatistikkNæringskodeRepository.hentNyesteKvartal(),
            sykefraværStatistikkVirksomhetRepository.hentNyesteKvartal()
        )
        val årstallOgKvartalForDvh = listOf(
            datavarehusLandRespository.hentSisteKvartal(),
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
            importSykefraværsstatistikkLand(gjeldendeÅrstallOgKvartal)
            importSykefraværsstatistikkSektor(gjeldendeÅrstallOgKvartal)
            importSykefraværsstatistikkNæring(gjeldendeÅrstallOgKvartal)
            importSykefraværsstatistikkNæring5siffer(gjeldendeÅrstallOgKvartal)
            importSykefraværsstatistikkNæringMedVarighet(gjeldendeÅrstallOgKvartal)
            importSykefraværsstatistikkVirksomhet(gjeldendeÅrstallOgKvartal)
            importSykefraværsstatistikkVirksomhetMedGradering(gjeldendeÅrstallOgKvartal)
            oppdaterPubliseringsstatus(gjeldendeÅrstallOgKvartal)
            gjeldendeÅrstallOgKvartal
        } else {
            log.info("Importerer ikke statistikk")
            gjeldendeÅrstallOgKvartal
        }
    }

    private fun oppdaterPubliseringsstatus(gjeldendeÅrstallOgKvartal: ÅrstallOgKvartal) {
        importtidspunktRepository.settInnImporttidspunkt(gjeldendeÅrstallOgKvartal)
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

    private fun importSykefraværsstatistikkLand(
        årstallOgKvartal: ÅrstallOgKvartal
    ): SlettOgOpprettResultat {

        val statistikk = datavarehusLandRespository.hentFor(årstallOgKvartal)

        val antallSlettet = sykefraværsstatistikkLandRepository.slettForKvartal(årstallOgKvartal)
        val antallOpprettet = sykefraværsstatistikkLandRepository.settInn(statistikk)

        val resultat = SlettOgOpprettResultat(antallSlettet, antallOpprettet)

        loggResultat(årstallOgKvartal, SlettOgOpprettResultat(antallSlettet, antallOpprettet), "land")
        return resultat
    }

    private fun importSykefraværsstatistikkSektor(
        årstallOgKvartal: ÅrstallOgKvartal
    ): SlettOgOpprettResultat {
        val sykefraværsstatistikkSektor =
            datavarehusLandRespository.hentSykefraværsstatistikkSektor(
                årstallOgKvartal,
            )

        val slettet = sykefraværStatistikkSektorRepository.slettDataFor(årstallOgKvartal)
        val opprettet = sykefraværStatistikkSektorRepository.settInn(sykefraværsstatistikkSektor)
        return SlettOgOpprettResultat(slettet, opprettet).also {
            loggResultat(årstallOgKvartal, it, "sektor")
        }
    }

    private fun importSykefraværsstatistikkNæring(
        årstallOgKvartal: ÅrstallOgKvartal
    ): SlettOgOpprettResultat {
        val sykefraværsstatistikkNæring = datavarehusRepository.hentSykefraværsstatistikkNæring(årstallOgKvartal)
        val slettet = sykefraværStatistikkNæringRepository.slettFor(årstallOgKvartal)
        val opprettet = sykefraværStatistikkNæringRepository.settInn(sykefraværsstatistikkNæring)
        val resultat = SlettOgOpprettResultat(slettet, opprettet)
        loggResultat(årstallOgKvartal, resultat, "næring")
        return resultat
    }

    private fun importSykefraværsstatistikkNæring5siffer(
        årstallOgKvartal: ÅrstallOgKvartal
    ): SlettOgOpprettResultat {
        val sykefraværsstatistikkNæringskode =
            datavarehusRepository.hentSykefraværsstatistikkNæring5siffer(årstallOgKvartal)

        val antallSlettet = sykefraværStatistikkNæringskodeRepository.slettKvartal(årstallOgKvartal)
        val antallOpprettet = sykefraværStatistikkNæringskodeRepository.settInn(sykefraværsstatistikkNæringskode)

        val resultat = SlettOgOpprettResultat(antallSlettet, antallOpprettet)

        loggResultat(årstallOgKvartal, resultat, "næring5siffer")
        return resultat
    }

    private fun importSykefraværsstatistikkVirksomhet(årstallOgKvartal: ÅrstallOgKvartal) {
        val statistikk: List<SykefraværsstatistikkVirksomhet> = if (environment.activeProfiles.contains("prod")) {
            datavarehusRepository.hentSykefraværsstatistikkVirksomhet(årstallOgKvartal)
        } else {
            SykefraværsstatistikkImporteringUtils.genererSykefraværsstatistikkVirksomhet(årstallOgKvartal)
        }
        val antallSlettet = sykefraværStatistikkVirksomhetRepository.slettForKvartal(årstallOgKvartal)
        val antallSattInn = sykefraværStatistikkVirksomhetRepository.settInn(statistikk)

        loggResultat(årstallOgKvartal, SlettOgOpprettResultat(antallSlettet, antallSattInn), "virksomhet")
    }

    private fun importSykefraværsstatistikkVirksomhetMedGradering(
        årstallOgKvartal: ÅrstallOgKvartal
    ) {
        val statistikk: List<SykefraværsstatistikkVirksomhetMedGradering> =
            if (environment.activeProfiles.contains("prod")) {
                datavarehusRepository.hentSykefraværsstatistikkVirksomhetMedGradering(årstallOgKvartal)
            } else {
                SykefraværsstatistikkImporteringUtils.genererSykefraværsstatistikkVirksomhetMedGradering(
                    årstallOgKvartal
                )
            }
        val antallSlettet = sykefraværStatistikkVirksomhetGraderingRepository.slettDataFor(årstallOgKvartal)
        val antallOprettet =
            sykefraværStatistikkVirksomhetGraderingRepository.settInn(
                statistikk
            )
        val resultat = SlettOgOpprettResultat(antallSlettet, antallOprettet)
        loggResultat(årstallOgKvartal, resultat, "virksomhet gradert sykemelding")
    }

    private fun importSykefraværsstatistikkNæringMedVarighet(
        årstallOgKvartal: ÅrstallOgKvartal
    ): SlettOgOpprettResultat {
        val data = datavarehusRepository.hentSykefraværsstatistikkNæringMedVarighet(årstallOgKvartal)

        val antallSlettet = sykefraværStatistikkNæringskodeMedVarighetRepository.slettKvartal(årstallOgKvartal)
        val antallOpprettet = sykefraværStatistikkNæringskodeMedVarighetRepository.settInn(data)

        val resultat = SlettOgOpprettResultat(antallSlettet, antallOpprettet)

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
        val første = årstallOgKvartal.firstOrNull() ?: return true
        return årstallOgKvartal.all { it == første }
    }
}
