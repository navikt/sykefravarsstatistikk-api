package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksport

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.VirksomhetMetadataRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.MetadataVirksomhetKafkamelding
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.Sektor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class EksporteringMetadataVirksomhetService(
    private val virksomhetMetadataRepository: VirksomhetMetadataRepository,
    private val kafkaService: KafkaService,
    @Value("\${statistikk.eksportering.aktivert}") val erEksporteringAktivert: Boolean
) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun eksporterMetadataVirksomhet(årstallOgKvartal: ÅrstallOgKvartal) {

        if (!erEksporteringAktivert) {
            log.info("Eksportering er ikke aktivert. Avbryter.")
        }

        log.info(
            "Starter eksportering av metadata (virksomhet) for årstall '{}' og kvartal '{}' på topic '{}'.",
            årstallOgKvartal.årstall,
            årstallOgKvartal.kvartal,
            KafkaTopic.SYKEFRAVARSSTATISTIKK_METADATA_V1.navn
        )

        val metadataVirksomhet =
            virksomhetMetadataRepository.hentVirksomhetMetadata(årstallOgKvartal)


        metadataVirksomhet.forEach { virksomhet ->
            try {
                val metadataVirksomhetKafkamelding = MetadataVirksomhetKafkamelding(
                    virksomhet.orgnr,
                    virksomhet.årstallOgKvartal,
                    virksomhet.primærnæring,
                    Bransjeprogram.finnBransje(virksomhet.primærnæringskode).getOrNull()?.type,
                    Sektor.fraSsbSektorkode(virksomhet.sektor.toInt())
                )

                kafkaService.sendMelding(
                    metadataVirksomhetKafkamelding,
                    KafkaTopic.SYKEFRAVARSSTATISTIKK_METADATA_V1,
                )
            } catch (ex: Exception) {
                log.error(
                    "Utsending av metadata for virksomhet med orgnr '{}' feilet: '{}'",
                    virksomhet.orgnr, ex.message
                )
            }
        }

        log.info(
            "Fullført eksportering av metadata (virksomhet) for årstall '{}' og kvartal '{}' på topic '{}'.",
            årstallOgKvartal.årstall,
            årstallOgKvartal.kvartal,
            KafkaTopic.SYKEFRAVARSSTATISTIKK_METADATA_V1.navn
        )
    }
}
