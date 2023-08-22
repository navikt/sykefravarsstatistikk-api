package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import lombok.extern.slf4j.Slf4j
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering.fjernDupliserteOrgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.EksporteringRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.GraderingRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.VirksomhetMetadataRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaUtsendingHistorikkRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Slf4j
@Component
class PostImporteringService(
    private val datavarehusRepository: DatavarehusRepository,
    private val virksomhetMetadataRepository: VirksomhetMetadataRepository,
    private val graderingRepository: GraderingRepository,
    private val eksporteringRepository: EksporteringRepository,
    private val kafkaUtsendingHistorikkRepository: KafkaUtsendingHistorikkRepository,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    object IngenRaderImportert

    fun overskrivMetadataForVirksomheter(årstallOgKvartal: ÅrstallOgKvartal): Either<IngenRaderImportert, Int> {
        val antallRaderOpprettet = overskrivVirksomhetMetadata(årstallOgKvartal)
        log.info(
            "Importering av $antallRaderOpprettet rader VirksomhetMetadata ferdig."
        )
        return if (antallRaderOpprettet > 0) {
            antallRaderOpprettet.right()
        } else {
            IngenRaderImportert.left()
        }
    }

    fun overskrivNæringskoderForVirksomheter(årstallOgKvartal: ÅrstallOgKvartal): Either<IngenRaderImportert, Int> {
        val antallRaderOpprettet = importVirksomhetNæringskode(årstallOgKvartal)
        log.info(
            "Importering av $antallRaderOpprettet rader med næringskodemappinger ferdig."
        )
        return if (antallRaderOpprettet > 0) {
            antallRaderOpprettet.right()
        } else {
            IngenRaderImportert.left()
        }
    }

    object ForrigeEksportIkkeFerdig

    @Deprecated("Brukes bare av legacy Kafka-strøm, som skal fases ut.")
    fun forberedNesteEksport(årstallOgKvartal: ÅrstallOgKvartal, slettHistorikk: Boolean): Either<ForrigeEksportIkkeFerdig, Int> {
        log.info("Forberede neste eksport: prosessen starter.")
        if (slettHistorikk) {
            val slettUtsendingHistorikkStart = System.currentTimeMillis()
            val antallRaderSlettetIUtsendingHistorikk = kafkaUtsendingHistorikkRepository.slettHistorikk()
            log.info(
                "Forberede neste eksport: utsending historikk (working table) har blitt nullstilt. "
                        + "{} rader har blitt slettet. Tok {} millis. ",
                antallRaderSlettetIUtsendingHistorikk,
                System.currentTimeMillis() - slettUtsendingHistorikkStart
            )
        } else {
            log.info("Forberede neste eksport: skal ikke slette historikk.")
        }
        val antallIkkeEksportertSykefaværsstatistikk = eksporteringRepository.hentAntallIkkeFerdigEksportert()
        if (antallIkkeEksportertSykefaværsstatistikk > 0) {
            log.warn(
                "Det finnes '{}' rader som IKKE er ferdig eksportert (eksportert=false). "
                        + "Skal ikke importere en ny liste av virksomheter i 'eksport_per_kvartal' da det ligger "
                        + "fortsatt noen rader markert som ikke eksportert. "
                        + "Du kan enten kjøre ferdig siste eksport eller oppdatere manuelt gjenstående rader "
                        + "med 'eksportert=true' i tabell 'eksport_per_kvartal'. "
                        + "Etter det kan du kjøre denne prosessen (forbered neste eksport) på nytt. ",
                antallIkkeEksportertSykefaværsstatistikk
            )
            // Vi er ikke ferdige med forrige eksport enda 💀
            return ForrigeEksportIkkeFerdig.left()
        }

        // Starter å forberede neste eksport:
        val antallSlettetEksportertPerKvartal = eksporteringRepository.slettEksportertPerKvartal()
        log.info(
            "Slettet '{}' rader fra forrige eksportering.",
            antallSlettetEksportertPerKvartal
        )
        val virksomhetMetadata = virksomhetMetadataRepository.hentVirksomhetMetadataMedNæringskoder(årstallOgKvartal)
        val virksomhetEksportPerKvartalListe = mapToVirksomhetEksportPerKvartal(virksomhetMetadata)
        log.info(
            "Skal gjøre klar '{}' virksomheter til neste eksportering. ",
            virksomhetEksportPerKvartalListe.size
        )
        val antallOpprettet = eksporteringRepository.opprettEksport(virksomhetEksportPerKvartalListe)
        log.info("Antall rader opprettet til neste eksportering: {}", antallOpprettet)
        return antallOpprettet.right()
    }

    private fun overskrivVirksomhetMetadata(årstallOgKvartal: ÅrstallOgKvartal): Int {
        val virksomheter = hentVirksomheterFraDvh(årstallOgKvartal)
        if (virksomheter.isEmpty()) {
            log.warn(
                "Stopper import av metadata. Fant ingen virksomheter for {}",
                årstallOgKvartal
            )
            return 0
        }
        log.info("Antall virksomheter fra DVH: {}", virksomheter.size)
        val antallSlettet = virksomhetMetadataRepository.slettVirksomhetMetadata()
        log.info(
            "Slettet '{}' VirksomhetMetadata for årstall '{}' og kvartal '{}'",
            antallSlettet,
            årstallOgKvartal.årstall,
            årstallOgKvartal.kvartal
        )
        val antallOpprettet = virksomhetMetadataRepository.opprettVirksomhetMetadata(
            mapToVirksomhetMetadata(virksomheter)
        )
        log.info("Antall rader VirksomhetMetadata opprettet: {}", antallOpprettet)
        return antallOpprettet
    }

    private fun hentVirksomheterFraDvh(årstallOgKvartal: ÅrstallOgKvartal): List<Orgenhet> {
        val virksomheter = datavarehusRepository.hentVirksomheter(årstallOgKvartal)
        if (virksomheter.isEmpty()) {
            log.warn(
                "Har ikke funnet noen virksomheter for årstall '{}' og kvartal '{}'. ",
                årstallOgKvartal.årstall,
                årstallOgKvartal.kvartal
            )
            return emptyList()
        }
        return fjernDupliserteOrgnr(virksomheter)
    }

    private fun importVirksomhetNæringskode(årstallOgKvartal: ÅrstallOgKvartal): Int {
        val virksomhetMetadataNæringskode5siffer =
            graderingRepository.hentVirksomhetMetadataNæringskode5siffer(årstallOgKvartal)
        if (virksomhetMetadataNæringskode5siffer.isEmpty()) {
            log.warn(
                "Ingen virksomhetMetadataNæringskode5siffer funnet i vår statistikktabell. Stopper import. "
            )
            return 0
        }
        val antallSlettetNæringskode5Siffer = virksomhetMetadataRepository.slettNæringOgNæringskode5siffer()
        log.info(
            "Slettet '{}' eksisterende NæringOgNæringskode5siffer. ", antallSlettetNæringskode5Siffer
        )
        val antallOpprettet = virksomhetMetadataRepository.opprettVirksomhetMetadataNæringskode5siffer(
            virksomhetMetadataNæringskode5siffer
        )
        log.info(
            "Antall rader VirksomhetMetadataNæringskode5siffer opprettet: {}",
            antallOpprettet
        )
        return antallOpprettet
    }

    companion object {
        private fun mapToVirksomhetMetadata(orgenhetList: List<Orgenhet>?): List<VirksomhetMetadata> {
            return orgenhetList!!.stream()
                .map { (orgnr, navn, rectype, sektor, næring, næringskode, årstallOgKvartal): Orgenhet ->
                    VirksomhetMetadata(
                        orgnr,
                        navn!!,
                        rectype!!,
                        sektor!!,
                        næring!!,
                        næringskode!!,
                        årstallOgKvartal
                    )
                }
                .collect(Collectors.toList())
        }

        private fun mapToVirksomhetEksportPerKvartal(
            virksomhetMetadataList: List<VirksomhetMetadata>
        ): List<VirksomhetEksportPerKvartal> {
            return virksomhetMetadataList.stream()
                .map { virksomhetMetadata: VirksomhetMetadata ->
                    VirksomhetEksportPerKvartal(
                        Orgnr(virksomhetMetadata.orgnr),
                        ÅrstallOgKvartal(
                            virksomhetMetadata.årstall, virksomhetMetadata.kvartal
                        ),
                        false
                    )
                }
                .collect(Collectors.toList())
        }
    }
}
