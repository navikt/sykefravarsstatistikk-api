package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.VirksomhetMetadata
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importAvSykefraværsstatistikk.fjernDupliserteOrgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.VirksomhetMetadataRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class VirksomhetMetadataService(
    private val kildeTilVirksomhetsdata: KildeTilVirksomhetsdata,
    private val virksomhetMetadataRepository: VirksomhetMetadataRepository,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    object IngenRaderImportert


    fun overskrivMetadataForVirksomheter(årstallOgKvartal: ÅrstallOgKvartal): Either<IngenRaderImportert, Int> {
        val virksomheter = kildeTilVirksomhetsdata.hentVirksomheter(årstallOgKvartal).fjernDupliserteOrgnr()

        if (virksomheter.isEmpty()) {
            log.warn(
                "Stopper import av metadata. Fant ingen virksomheter for $årstallOgKvartal.",
            )
            return IngenRaderImportert.left()
        }

        log.info("Antall virksomheter til import: {}", virksomheter.size)

        val antallSlettet = virksomhetMetadataRepository.slettVirksomhetMetadata()

        log.info(
            "Slettet '{}' rader VirksomhetMetadata for {}'",
            antallSlettet,
            årstallOgKvartal,
        )

        val antallOpprettet = virksomhetMetadataRepository.opprettVirksomhetMetadata(
            virksomheter.map { it.tilDomene() }
        )

        log.info("Antall rader VirksomhetMetadata opprettet: {}", antallOpprettet)
        log.info("Importering av $antallOpprettet rader VirksomhetMetadata ferdig.")

        return if (antallOpprettet > 0) {
            antallOpprettet.right()
        } else {
            IngenRaderImportert.left()
        }
    }

}
