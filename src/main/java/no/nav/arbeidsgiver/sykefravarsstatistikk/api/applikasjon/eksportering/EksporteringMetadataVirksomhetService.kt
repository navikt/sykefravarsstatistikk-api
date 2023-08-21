package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransjeprogram
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.KafkaTopic
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.VirksomhetMetadataRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaClient
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto.MetadataVirksomhetKafkamelding
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.dto.Sektor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class EksporteringMetadataVirksomhetService(
    private val virksomhetMetadataRepository: VirksomhetMetadataRepository,
    private val kafkaClient: KafkaClient,
) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun eksporterMetadataVirksomhet(årstallOgKvartal: ÅrstallOgKvartal): Either<FantIkkeData, Unit> {

        log.info(
            "Starter eksportering av metadata (virksomhet) for årstall '{}' og kvartal '{}' på topic '{}'.",
            årstallOgKvartal.årstall,
            årstallOgKvartal.kvartal,
            KafkaTopic.SYKEFRAVARSSTATISTIKK_METADATA_V1.navn
        )

        val metadataVirksomhet =
            virksomhetMetadataRepository.hentVirksomhetMetadata(årstallOgKvartal)

        if (metadataVirksomhet.isEmpty()) {
            return FantIkkeData.left()
        }


        metadataVirksomhet.forEach { virksomhet ->
            try {
                val metadataVirksomhetKafkamelding = MetadataVirksomhetKafkamelding(
                    virksomhet.orgnr,
                    virksomhet.årstallOgKvartal,
                    virksomhet.primærnæring,
                    Bransjeprogram.finnBransje(virksomhet.primærnæringskode).getOrNull()?.type,
                    Sektor.fraSsbSektorkode(virksomhet.sektor.toInt())
                )

                kafkaClient.sendMelding(
                    metadataVirksomhetKafkamelding,
                    KafkaTopic.SYKEFRAVARSSTATISTIKK_METADATA_V1,
                )
            } catch (ex: Exception) {
                log.error(
                    "Utsending av metadata for virksomhet med orgnr '{}' feilet: '{}'",
                    virksomhet.orgnr, ex.message
                )

                throw ex
            }
        }

        log.info(
            "Fullført eksportering av metadata (virksomhet) for årstall '{}' og kvartal '{}' på topic '{}'.",
            årstallOgKvartal.årstall,
            årstallOgKvartal.kvartal,
            KafkaTopic.SYKEFRAVARSSTATISTIKK_METADATA_V1.navn
        )
        return Unit.right()
    }

    object FantIkkeData
}
