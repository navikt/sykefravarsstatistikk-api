package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoer

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.ImporttidspunktRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.PubliseringsdatoerRepository
import org.springframework.stereotype.Service

@Service
class PubliseringsdatoerService(
    private val publiseringsdatoerRepository: PubliseringsdatoerRepository,
    private val importtidspunktRepository: ImporttidspunktRepository
) {

    fun hentPubliseringsdatoer(): PubliseringskalenderDto? {
        val publiseringsdatoer = publiseringsdatoerRepository.hentPubliseringsdatoer()
        val nyesteImport = importtidspunktRepository.hentNyesteImporterteKvartal()
            ?: return null

        val nestePubliseringsdato =
            publiseringsdatoer
                .filter { it.offentligDato.isAfter(nyesteImport.sistImportertTidspunkt) }
                .minOfOrNull { it.offentligDato } // Dette blir vel feil hvis importtidspunktet ikk er oppdatert?

        return PubliseringskalenderDto(
            sistePubliseringsdato = nyesteImport.sistImportertTidspunkt,
            gjeldendePeriode = nyesteImport.gjeldendePeriode,
            nestePubliseringsdato = nestePubliseringsdato,
        )
    }
}
