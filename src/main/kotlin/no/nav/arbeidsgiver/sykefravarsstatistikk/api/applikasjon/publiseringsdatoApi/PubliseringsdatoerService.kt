package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoApi

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.ImporttidspunktRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.PubliseringsdatoerRepository
import org.springframework.stereotype.Service

@Service
class PubliseringsdatoerService(
    private val publiseringsdatoerRepository: PubliseringsdatoerRepository,
    private val importtidspunktRepository: ImporttidspunktRepository
) {

    fun hentPubliseringsdatoer(): Publiseringsdatoer? {
        val publiseringsdatoer = publiseringsdatoerRepository.hentPubliseringsdatoer()
        val nyesteImport = importtidspunktRepository.hentNyesteImporterteKvartal()
            ?: return null

        val nestePubliseringsdato =
            publiseringsdatoer
                .filter { it.offentligDato.isAfter(nyesteImport.sistImportertTidspunkt) }
                .minOfOrNull { it.offentligDato }

        return Publiseringsdatoer(
            sistePubliseringsdato = nyesteImport.sistImportertTidspunkt,
            gjeldendePeriode = nyesteImport.gjeldendePeriode,
            nestePubliseringsdato = nestePubliseringsdato
        )
    }
}
