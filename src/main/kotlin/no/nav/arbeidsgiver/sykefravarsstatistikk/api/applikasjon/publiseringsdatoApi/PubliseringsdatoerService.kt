package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoApi

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.ImporttidspunktRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.PubliseringsdatoerRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.ResponseStatus
import java.time.LocalDate

@Service
class PubliseringsdatoerService(
    private val publiseringsdatoerRepository: PubliseringsdatoerRepository,
    private val importtidspunktRepository: ImporttidspunktRepository
) {

    fun hentSistePubliserteKvartal(): ÅrstallOgKvartal {
        return importtidspunktRepository.hentNyesteImporterteKvartal()?.gjeldendePeriode
            ?: throw PubliseringsdatoerDatauthentingFeil("Kunne ikke hente ut siste publiserte kvartal")
    }

    fun hentPubliseringsdatoer(): Publiseringsdatoer? {
        val publiseringsdatoer = publiseringsdatoerRepository.hentPubliseringsdatoer()
        val nyesteImport = importtidspunktRepository.hentNyesteImporterteKvartal()
            ?: return null

        val nestePubliseringsdato =
            finnNestePubliseringsdato(publiseringsdatoer, nyesteImport.sistImportertTidspunkt)

        return Publiseringsdatoer(
            sistePubliseringsdato = nyesteImport.sistImportertTidspunkt,
            gjeldendePeriode = nyesteImport.gjeldendePeriode,
            nestePubliseringsdato = nestePubliseringsdato
        )
    }

    private fun finnNestePubliseringsdato(
        publiseringsdatoer: List<Publiseringsdato>, forrigeImporttidspunkt: LocalDate
    ) = publiseringsdatoer
        .filter { it.offentligDato.isAfter(forrigeImporttidspunkt) }
        .minOfOrNull { it.offentligDato }
}


@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
class PubliseringsdatoerDatauthentingFeil(message: String?) : RuntimeException(message)
