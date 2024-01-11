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


    private fun overskrivVirksomhetMetadata(årstallOgKvartal: ÅrstallOgKvartal): Int {
        val virksomheter = kildeTilVirksomhetsdata.hentVirksomheter(årstallOgKvartal).fjernDupliserteOrgnr()
        if (virksomheter.isEmpty()) {
            log.warn(
                "Stopper import av metadata. Fant ingen virksomheter for $årstallOgKvartal.",
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
            virksomheter.map {
                VirksomhetMetadata(
                    it.orgnr,
                    it.navn!!,
                    it.rectype!!,
                    it.sektor!!,
                    it.næring!!,
                    it.næringskode!!,
                    it.årstallOgKvartal
                )
            }
        )
        log.info("Antall rader VirksomhetMetadata opprettet: {}", antallOpprettet)
        return antallOpprettet
    }
}
