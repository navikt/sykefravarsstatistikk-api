package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoApi

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.ImporttidspunktRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.PubliseringsdatoerRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PubliseringsdatoerService(
    private val publiseringsdatoerRepository: PubliseringsdatoerRepository,
    private val importtidspunktRepository: ImporttidspunktRepository
) {

    fun hentSistePubliserteKvartal(): ÅrstallOgKvartal {
        return importtidspunktRepository.hentNyesteImporterteKvartal()?.gjeldendePeriode
            ?: throw PubliseringsdatoerDatauthentingFeil("Kunne ikke hente ut siste publiseringstidspunkt")
    }

    fun hentPubliseringsdatoer(): PubliseringsdatoerJson {
        val publiseringsdatoer = publiseringsdatoerRepository.hentPubliseringsdatoer()
        val sisteImporttidspunkt = importtidspunktRepository.hentNyesteImporterteKvartal()
            ?: throw PubliseringsdatoerDatauthentingFeil("Klarte ikke hente forrige publiseringstidspunkt fra databasen")

        val nestePubliseringsdato =
            finnNestePubliseringsdato(publiseringsdatoer, sisteImporttidspunkt.sistImportertTidspunkt)

        return PubliseringsdatoerJson(
            sistePubliseringsdato = sisteImporttidspunkt.sistImportertTidspunkt.toString(),
            gjeldendePeriode = sisteImporttidspunkt.gjeldendePeriode,
            nestePubliseringsdato = nestePubliseringsdato?.toString()
                ?: "Neste publiseringsdato er utilgjengelig"
        )
    }

    private fun finnNestePubliseringsdato(
        publiseringsdatoer: List<Publiseringsdato>, forrigeImporttidspunkt: LocalDate
    ) = publiseringsdatoer
        .filter { it.offentligDato.isAfter(forrigeImporttidspunkt) }
        .minOfOrNull { it.offentligDato }
}
