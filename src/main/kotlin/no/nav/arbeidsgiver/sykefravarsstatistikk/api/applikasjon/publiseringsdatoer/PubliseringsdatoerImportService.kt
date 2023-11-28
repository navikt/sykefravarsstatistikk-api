package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.PubliseringsdatoerRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusPubliseringsdatoerRepository
import org.springframework.stereotype.Component

@Component
class PubliseringsdatoerImportService(
    private val publiseringsdatoerRepository: PubliseringsdatoerRepository,
    private val datavarehusPubliseringsdatoerRepository: DatavarehusPubliseringsdatoerRepository
) {
    fun importerDatoerFraDatavarehus() {
        val publiseringsdatoerFraDvh = datavarehusPubliseringsdatoerRepository.hentPubliseringsdatoerFraDvh()
        publiseringsdatoerRepository.overskrivPubliseringsdatoer(publiseringsdatoerFraDvh)
    }
}
