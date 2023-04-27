package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.EksporteringRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.VirksomhetMetadataRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaTopicNavn
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.KafkaService
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.MetadataVirksomhetKafkamelding
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka.dto.Sektor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger
import kotlin.jvm.optionals.getOrNull

@Service
class EksporteringMetadataVirksomhetService(
    val eksporteringRepository: EksporteringRepository,
    val virksomhetMetadataRepository: VirksomhetMetadataRepository,
    val kafkaService: KafkaService,
    @Value("\${statistikk.eksportering.aktivert}") val erEksporteringAktivert: Boolean
) {
    val log = LoggerFactory.getLogger(this::class.java)

    fun eksporterMetadataVirksomhet(årstallOgKvartal: ÅrstallOgKvartal) {
        if (!erEksporteringAktivert) {
            log.info("Eksportering er ikke aktivert. Avbryter.")
        }

        log.info(
            "Starter eksportering av metadata (virksomhet) for årstall '{}' og kvartal '{}' på topic '{}'.",
            årstallOgKvartal.årstall,
            årstallOgKvartal.kvartal,
            KafkaTopicNavn.SYKEFRAVARSSTATISTIKK_METADATA_V1.topic
        )

        val antallEksportert = AtomicInteger()
        val antallIkkeEksportert = AtomicInteger()

        val metadataVirksomhet =
            virksomhetMetadataRepository.hentVirksomhetMetadata(årstallOgKvartal)


        metadataVirksomhet.forEach {virksomhet ->
            if (virksomhet.orgnr == null) {
                log.error("Orgnummer er 'null'")
                return@forEach
            }

            var erSendt = false

            // TODO: Finn primærnæringskoden, aka den som ligger først i enhetsregisteret.
            val næringskode = velgPrimærnæringskode(virksomhet.næringOgNæringskode5siffer.map { it.næringskode5Siffer });

            val metadataVirksomhetKafkamelding = MetadataVirksomhetKafkamelding(
                virksomhet.orgnr!!,
                virksomhet.årstallOgKvartal,
                næringskode.substring(0, 2),
                Bransjeprogram.finnBransje(næringskode).getOrNull()?.type,
                Sektor.valueOf(virksomhet.sektor)
            )

            kafkaService.send(
                metadataVirksomhetKafkamelding,
                KafkaTopicNavn.SYKEFRAVARSSTATISTIKK_METADATA_V1
            )

            if (erSendt) {
                antallEksportert.incrementAndGet()
            } else {
                antallIkkeEksportert.incrementAndGet()
            }

        }
    }
}
