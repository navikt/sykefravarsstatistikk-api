package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.common.collect.Lists
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaClient
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaUtsendingException
import org.slf4j.LoggerFactory
import org.springframework.kafka.KafkaException
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

@Component
@Deprecated("Brukes bare av legacy Kafka-strøm, som skal fases ut.")
class EksporteringService(
    private val legacyEksporteringRepository: LegacyEksporteringRepository,
    private val virksomhetMetadataRepository: VirksomhetMetadataRepository,
    private val sykefraværsstatistikkTilEksporteringRepository: SykefraværsstatistikkTilEksporteringRepository,
    private val sykefraværStatistikkLandRepository: SykefraværStatistikkLandRepository,
    private val sykefraværStatistikkSektorRepository: SykefraværStatistikkSektorRepository,
    private val kafkaClient: KafkaClient,
    private val sykefravarStatistikkVirksomhetRepository: SykefravarStatistikkVirksomhetRepository,
    private val sykefraværStatistikkNæringRepository: SykefraværStatistikkNæringRepository,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Deprecated("Bruk eksport per kategori")
    fun legacyEksporter(
        årstallOgKvartal: ÅrstallOgKvartal,
    ): Either<LegacyEksportFeil, Int> {
        val virksomheterTilEksport = LegacyEksporteringServiceUtils.getListeAvVirksomhetEksportPerKvartal(
            årstallOgKvartal, legacyEksporteringRepository
        )
        val antallStatistikkSomSkalEksporteres =
            if (virksomheterTilEksport.isEmpty()) 0 else virksomheterTilEksport.size

        if (antallStatistikkSomSkalEksporteres == 0) {
            log.info(
                "Ingen statistikk å eksportere for årstall '{}' og kvartal '{}'.",
                årstallOgKvartal.årstall,
                årstallOgKvartal.kvartal
            )
            return LegacyEksportFeil.IngenNyStatistikk.left()
        }
        log.info(
            "Starting eksportering for årstall '{}' og kvartal '{}'. Skal eksportere '{}' rader med statistikk.",
            årstallOgKvartal.årstall,
            årstallOgKvartal.kvartal,
            antallStatistikkSomSkalEksporteres
        )
        val antallEksporterteVirksomheter = runCatching {
            legacyEksporter(virksomheterTilEksport, årstallOgKvartal)
        }.getOrElse {
            when (it) {
                is KafkaUtsendingException, is KafkaException -> {
                    log.warn("Fikk exception fra Kafka med melding: '{}'. Avbryter prosess.", it.message, it)
                    return LegacyEksportFeil.EksportFeilet.left()
                }

                else -> throw it
            }
        }
        return antallEksporterteVirksomheter.right()
    }

    sealed class LegacyEksportFeil {
        data object EksportFeilet : LegacyEksportFeil()
        data object IngenNyStatistikk : LegacyEksportFeil()
    }

    @Throws(KafkaUtsendingException::class)
    @Deprecated("Brukes bare av legacy Kafka-strøm, som skal fases ut.")
    protected fun legacyEksporter(
        virksomheterTilEksport: List<VirksomhetEksportPerKvartal>, årstallOgKvartal: ÅrstallOgKvartal
    ): Int {
        val startEksportering = System.currentTimeMillis()
        kafkaClient.nullstillUtsendingRapport(
            virksomheterTilEksport.size, KafkaTopic.SYKEFRAVARSSTATISTIKK_V1
        )
        val virksomhetMetadataListe =
            virksomhetMetadataRepository.hentVirksomhetMetadataMedNæringskoder(årstallOgKvartal)
        val umaskertSykefraværsstatistikkSistePublisertKvartalLand =
            sykefraværStatistikkLandRepository.hentForKvartaler(listOf(årstallOgKvartal))
        val sykefraværsstatistikkSektor =
            sykefraværStatistikkSektorRepository.hentForKvartaler(listOf(årstallOgKvartal))
        val sykefraværsstatistikkForNæringer =
            sykefraværStatistikkNæringRepository.hentForAlleNæringer(
                listOf(årstallOgKvartal)
            )
        val sykefraværsstatistikkForNæringskoder =
            sykefraværsstatistikkTilEksporteringRepository.hentSykefraværprosentForAlleNæringskoder(
                årstallOgKvartal
            )
        val sykefraværsstatistikkVirksomhetUtenVarighet =
            sykefravarStatistikkVirksomhetRepository.hentSykefraværAlleVirksomheter(listOf(årstallOgKvartal))
        val umaskertSykefraværForEttKvartal = LegacyEksporteringServiceUtils.hentSisteKvartalIBeregningen(
            umaskertSykefraværsstatistikkSistePublisertKvartalLand, årstallOgKvartal
        )
        val landSykefravær = LegacyEksporteringServiceUtils.getSykefraværMedKategoriForLand(
            årstallOgKvartal,
            LegacyEksporteringServiceUtils.mapToSykefraværsstatistikkLand(
                umaskertSykefraværForEttKvartal!!
            )
        )
        val virksomhetMetadataMap = LegacyEksporteringServiceUtils.getVirksomhetMetadataHashMap(virksomhetMetadataListe)
        val subsets = Lists.partition(virksomheterTilEksport, LegacyEksporteringServiceUtils.EKSPORT_BATCH_STØRRELSE)
        val antallEksportert = AtomicInteger()
        subsets.forEach { subset: List<VirksomhetEksportPerKvartal> ->
            val virksomheterMetadataIDenneSubset =
                LegacyEksporteringServiceUtils.getVirksomheterMetadataFraSubset(virksomhetMetadataMap, subset)
            log.info(
                "Starter utsending av '{}' statistikk meldinger (fra '{}' virksomheter)",
                virksomheterMetadataIDenneSubset.size,
                subset.size
            )
            sendIBatch(
                virksomheterMetadataIDenneSubset,
                årstallOgKvartal,
                sykefraværsstatistikkSektor,
                sykefraværsstatistikkForNæringer,
                sykefraværsstatistikkForNæringskoder,
                sykefraværsstatistikkVirksomhetUtenVarighet,
                landSykefravær,
                antallEksportert,
                virksomheterTilEksport.size
            )
        }
        val stoptEksportering = System.currentTimeMillis()
        val totalProsesseringTidISekunder = (stoptEksportering - startEksportering) / 1000
        log.info(
            "Eksportering er ferdig med: antall statistikk for virksomhet prosessert='{}', "
                    + "Eksportering tok '{}' sekunder totalt. "
                    + "Snitt prossesseringstid ved utsending til Kafka er: '{}'. "
                    + "Snitt prossesseringstid for å oppdatere DB er: '{}'.",
            kafkaClient.antallMeldingerMottattForUtsending,
            totalProsesseringTidISekunder,
            kafkaClient.snittTidUtsendingTilKafka,
            kafkaClient.snittTidOppdateringIDB
        )
        log.info("[Måling] Rå data ved måling: {}", kafkaClient.råDataVedDetaljertMåling)
        return antallEksportert.get()
    }

    private fun sendIBatch(
        virksomheterMetadata: List<VirksomhetMetadata>,
        årstallOgKvartal: ÅrstallOgKvartal,
        sykefraværsstatistikkSektor: List<SykefraværsstatistikkSektor>,
        sykefraværsstatistikkForNæring: List<SykefraværsstatistikkForNæring>,
        sykefraværsstatistikkForNæringskode: List<SykefraværsstatistikkForNæringskode>,
        sykefraværsstatistikkVirksomhetUtenVarighet: List<SykefraværsstatistikkVirksomhetUtenVarighet>,
        landSykefravær: SykefraværMedKategori,
        antallEksportert: AtomicInteger,
        antallTotaltStatistikk: Int
    ) {
        val antallSentTilEksport = AtomicInteger()
        val antallVirksomheterLagretSomEksportertIDb = AtomicInteger()
        val eksporterteVirksomheterListe: MutableList<String> = ArrayList()
        val sykefraværsstatistikkVirksomhetForEttKvartalUtenVarighetMap = LegacyEksporteringServiceUtils.toMap(
            LegacyEksporteringServiceUtils.filterByKvartal(
                årstallOgKvartal,
                sykefraværsstatistikkVirksomhetUtenVarighet
            )
        )
        virksomheterMetadata.forEach(
            Consumer { virksomhetMetadata: VirksomhetMetadata? ->
                val startUtsendingProcess = System.nanoTime()
                if (virksomhetMetadata != null) {
                    kafkaClient.send(
                        årstallOgKvartal,
                        LegacyEksporteringServiceUtils.getVirksomhetSykefravær(
                            virksomhetMetadata,
                            sykefraværsstatistikkVirksomhetForEttKvartalUtenVarighetMap
                        ),
                        LegacyEksporteringServiceUtils.getSykefraværMedKategoriForNæring5Siffer(
                            virksomhetMetadata, sykefraværsstatistikkForNæringskode
                        ),
                        LegacyEksporteringServiceUtils.getSykefraværMedKategoriNæringForVirksomhet(
                            virksomhetMetadata, sykefraværsstatistikkForNæring
                        ),
                        LegacyEksporteringServiceUtils.getSykefraværMedKategoriForSektor(
                            virksomhetMetadata,
                            sykefraværsstatistikkSektor
                        ),
                        landSykefravær
                    )
                    val stopUtsendingProcess = System.nanoTime()
                    antallSentTilEksport.getAndIncrement()
                    kafkaClient.addUtsendingTilKafkaProcessingTime(
                        startUtsendingProcess, stopUtsendingProcess
                    )
                    val antallVirksomhetertLagretSomEksportert =
                        LegacyEksporteringServiceUtils.leggTilOrgnrIEksporterteVirksomheterListaOglagreIDbNårListaErFull(
                            virksomhetMetadata.orgnr,
                            årstallOgKvartal,
                            eksporterteVirksomheterListe,
                            legacyEksporteringRepository,
                            kafkaClient
                        )
                    antallVirksomheterLagretSomEksportertIDb.addAndGet(
                        antallVirksomhetertLagretSomEksportert
                    )
                }
            })
        val antallRestendeOppdatert = LegacyEksporteringServiceUtils.lagreEksporterteVirksomheterOgNullstillLista(
            årstallOgKvartal, eksporterteVirksomheterListe, legacyEksporteringRepository, kafkaClient
        )
        antallVirksomheterLagretSomEksportertIDb.addAndGet(antallRestendeOppdatert)
        val eksportertHittilNå = antallEksportert.addAndGet(antallSentTilEksport.get())
        LegacyEksporteringServiceUtils.cleanUpEtterBatch(legacyEksporteringRepository)
        log.info(
            String.format(
                "Eksportert '%d' rader av '%d' totalt ('%d' oppdatert i DB)",
                eksportertHittilNå,
                antallTotaltStatistikk,
                antallVirksomheterLagretSomEksportertIDb.get()
            )
        )
    }
}