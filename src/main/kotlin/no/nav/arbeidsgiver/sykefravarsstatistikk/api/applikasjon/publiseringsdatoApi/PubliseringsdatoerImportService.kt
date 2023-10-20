package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoApi

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.PubliseringsdatoerRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
import org.springframework.stereotype.Component

@Component
class PubliseringsdatoerImportService(
    private val publiseringsdatoerRepository: PubliseringsdatoerRepository,
    private val datavarehusRepository: DatavarehusRepository
) {
    fun importerDatoerFraDatavarehus() {
        val publiseringsdatoerFraDvh = datavarehusRepository.hentPubliseringsdatoerFraDvh()
        publiseringsdatoerRepository.oppdaterPubliseringsdatoer(publiseringsdatoerFraDvh)
    }
}
