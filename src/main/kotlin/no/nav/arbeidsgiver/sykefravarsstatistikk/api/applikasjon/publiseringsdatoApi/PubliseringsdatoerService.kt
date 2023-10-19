package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.publiseringsdatoApi

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.PubliseringsdatoerRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PubliseringsdatoerService(private val publiseringsdatoerRepository: PubliseringsdatoerRepository) {

    fun hentSistePubliserteKvartal(): ÅrstallOgKvartal {
        return publiseringsdatoerRepository.hentSisteImporttidspunkt()?.gjeldendePeriode
            ?: throw PubliseringsdatoerDatauthentingFeil("Kunne ikke hente ut siste publiseringstidspunkt")
    }

    fun hentPubliseringsdatoer(): PubliseringsdatoerJson? {
        val publiseringsdatoer = publiseringsdatoerRepository.hentPubliseringsdatoer()
        val sisteImporttidspunkt = publiseringsdatoerRepository.hentSisteImporttidspunkt()
            ?: throw PubliseringsdatoerDatauthentingFeil("Klarte ikke hente forrige publiseringstidspunkt fra databasen")

        val nestePubliseringsdato = finnNestePubliseringsdato(publiseringsdatoer, sisteImporttidspunkt.importertDato)

        return PubliseringsdatoerJson(
            sistePubliseringsdato = sisteImporttidspunkt.importertDato.toString(),
            gjeldendePeriode = sisteImporttidspunkt.gjeldendePeriode,
            nestePubliseringsdato = nestePubliseringsdato?.toString()
                ?: "Neste publiseringsdato er utilgjengelig"
        )
    }
}

private fun finnNestePubliseringsdato(
    publiseringsdatoer: List<Publiseringsdato>, forrigeImporttidspunkt: LocalDate
) = publiseringsdatoer
    .filter { it.offentligDato.toLocalDate().isAfter(forrigeImporttidspunkt) }
    .minOfOrNull { it.offentligDato.toLocalDate() }
