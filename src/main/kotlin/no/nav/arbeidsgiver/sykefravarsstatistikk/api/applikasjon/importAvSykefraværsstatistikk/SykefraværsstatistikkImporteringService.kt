package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhet
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.SykefraværsstatistikkVirksomhetMedGradering
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.domene.SlettOgOpprettResultat
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.*
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class SykefraværsstatistikkImporteringService(
    private val sykefraværStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository,
    private val importtidspunktRepository: ImporttidspunktRepository,
    private val sykefraværsstatistikkLandRepository: SykefraværStatistikkLandRepository,
    private val sykefraværStatistikkVirksomhetGraderingRepository: SykefravarStatistikkVirksomhetGraderingRepository,
    private val sykefraværStatistikkNæringskodeMedVarighetRepository: SykefraværStatistikkNæringskodeMedVarighetRepository,
    private val sykefraværStatistikkSektorRepository: SykefraværStatistikkSektorRepository,
    private val sykefraværStatistikkNæringRepository: SykefraværStatistikkNæringRepository,
    private val sykefraværStatistikkNæringskodeRepository: SykefraværStatistikkNæringskodeRepository,
    private val datavarehusLandRespository: DatavarehusLandRespository,
    private val datavarehusNæringRepository: DatavarehusNæringRepository,
    private val datavarehusNæringskodeRepository: DatavarehusNæringskodeRepository,
    private val datavarehusAggregertRepositoryV1: DatavarehusAggregertRepositoryV1,
    private val datavarehusAggregertRepositoryV2: DatavarehusAggregertRepositoryV2,
    private val environment: Environment,
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    fun importerHvisDetFinnesNyStatistikk() {
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
            datavarehusNæringRepository.hentSisteKvartal(),
            datavarehusNæringskodeRepository.hentSisteKvartal(),
            datavarehusAggregertRepositoryV1.hentSisteKvartal(),
        )
        val gjeldendeKvartal = årstallOgKvartalForDvh[0]
        if (kanImportStartes(årstallOgKvartalForSykefraværsstatistikk, årstallOgKvartalForDvh)) {
            log.info("Importerer ny statistikk")
            importerAlleKategorier(gjeldendeKvartal)
            importtidspunktRepository.settInnImporttidspunkt(gjeldendeKvartal)
        }
    }

    fun importerAlleKategorier(gjeldendekvartal: ÅrstallOgKvartal) {
        importSykefraværsstatistikkLand(gjeldendekvartal)
        importSykefraværsstatistikkSektor(gjeldendekvartal)
        importSykefraværsstatistikkNæring(gjeldendekvartal)
        importSykefraværsstatistikkNæring5siffer(gjeldendekvartal)
        importSykefraværsstatistikkNæringMedVarighet(gjeldendekvartal)
        importSykefraværsstatistikkVirksomhet(gjeldendekvartal)
        importSykefraværsstatistikkVirksomhetMedGradering(gjeldendekvartal)
    }


    fun kanImportStartes(
        årstallOgKvartalForSfsDb: List<ÅrstallOgKvartal>,
        årstallOgKvartalForDvh: List<ÅrstallOgKvartal>
    ): Boolean {
        val allStatistikkFraDvhHarSammeÅrstallOgKvartal =
            årstallOgKvartalForDvh.all { it == årstallOgKvartalForDvh.firstOrNull() }

        if (!allStatistikkFraDvhHarSammeÅrstallOgKvartal) {
            log.warn(
                "Kunne ikke importere ny statistikk, tabellene hadde forskjellige årstall og kvartal. "
                        + "Kvartaler Sykefraværsstatistikk-DB: {}. Kvartaler DVH: {}",
                årstallOgKvartalForSfsDb,
                årstallOgKvartalForDvh
            )
            return false
        }

        val sisteÅrstallOgKvartalForDvh = årstallOgKvartalForDvh[0]
        val importertStatistikkLiggerEttKvartalBakDvh =
            (sisteÅrstallOgKvartalForDvh.minusKvartaler(1) == årstallOgKvartalForSfsDb.min())

        return if (importertStatistikkLiggerEttKvartalBakDvh) {
            log.info(
                "Skal importere statistikk fra Dvh for årstall {} og kvartal {}",
                sisteÅrstallOgKvartalForDvh.årstall,
                sisteÅrstallOgKvartalForDvh.kvartal
            )
            true
        } else if (sisteÅrstallOgKvartalForDvh == årstallOgKvartalForSfsDb.min()) {
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
                årstallOgKvartalForSfsDb.min().årstall,
                årstallOgKvartalForSfsDb.min().kvartal
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
        val sykefraværsstatistikkNæring = datavarehusNæringRepository.hentFor(årstallOgKvartal)
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
            datavarehusNæringskodeRepository.hentFor(årstallOgKvartal)

        val antallSlettet = sykefraværStatistikkNæringskodeRepository.slettKvartal(årstallOgKvartal)
        val antallOpprettet = sykefraværStatistikkNæringskodeRepository.settInn(sykefraværsstatistikkNæringskode)

        val resultat = SlettOgOpprettResultat(antallSlettet, antallOpprettet)

        loggResultat(årstallOgKvartal, resultat, "næring5siffer")
        return resultat
    }

    fun importSykefraværsstatistikkVirksomhet(årstallOgKvartal: ÅrstallOgKvartal) {
        val statistikk: List<SykefraværsstatistikkVirksomhet> = if (environment.activeProfiles.contains("dev")) {
            SykefraværsstatistikkTestdatagenerator.genererSykefraværsstatistikkVirksomhet(årstallOgKvartal)
        } else {
            datavarehusAggregertRepositoryV1.hentSykefraværsstatistikkVirksomhet(årstallOgKvartal)
        }
        val antallSlettet = sykefraværStatistikkVirksomhetRepository.slettForKvartal(årstallOgKvartal)
        val antallSattInn = sykefraværStatistikkVirksomhetRepository.settInn(statistikk)

        loggResultat(årstallOgKvartal, SlettOgOpprettResultat(antallSlettet, antallSattInn), "virksomhet")
    }

    fun importSykefraværsstatistikkVirksomhetMedGradering(
        årstallOgKvartal: ÅrstallOgKvartal
    ) {
        val statistikk: List<SykefraværsstatistikkVirksomhetMedGradering> =
            if (environment.activeProfiles.contains("dev")) {
                SykefraværsstatistikkTestdatagenerator.genererSykefraværsstatistikkVirksomhetMedGradering(
                    årstallOgKvartal
                )
            } else {
                datavarehusAggregertRepositoryV2.hentSykefraværsstatistikkVirksomhetMedGradering(
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
        val data = datavarehusAggregertRepositoryV1.hentSykefraværsstatistikkNæringMedVarighet(
            årstallOgKvartal
        )

        val antallSlettet = sykefraværStatistikkNæringskodeMedVarighetRepository.slettKvartal(årstallOgKvartal)
        val antallOpprettet = sykefraværStatistikkNæringskodeMedVarighetRepository.settInn(data)

        val resultat = SlettOgOpprettResultat(antallSlettet, antallOpprettet)

        loggResultat(årstallOgKvartal, resultat, "næring med varighet")
        return resultat
    }

    private fun loggResultat(
        årstallOgKvartal: ÅrstallOgKvartal, resultat: SlettOgOpprettResultat, type: String
    ) {
        val melding = if (resultat.antallRadOpprettet == 0 && resultat.antallRadSlettet == 0) {
            "Ingenting har blitt slettet eller importert."
        } else {
            "Antall rader opprettet: ${resultat.antallRadOpprettet}, antall slettet: ${resultat.antallRadSlettet}"
        }
        log.info("Import av sykefraværsstatistikk av type $type for $årstallOgKvartal i miljø $environment er ferdig: $melding")
    }
}
