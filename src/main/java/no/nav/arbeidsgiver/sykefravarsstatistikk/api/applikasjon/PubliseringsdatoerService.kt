package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon

import io.vavr.control.Option
import io.vavr.control.Try
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.PubliseringsdatoerRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.stream.Collectors

@Service
class PubliseringsdatoerService(private val publiseringsdatoerRepository: PubliseringsdatoerRepository) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun hentSistePubliserteKvartal(): ÅrstallOgKvartal {
        return publiseringsdatoerRepository.hentSisteImporttidspunkt()
            .getOrElseThrow { PubliseringsdatoerDatauthentingFeil("Kunne ikke hente ut siste publiseringstidspunkt") }
            .gjeldendePeriode
    }

    fun hentPubliseringsdatoer(): Option<Publiseringsdatoer> {
        val publiseringsdatoer = publiseringsdatoerRepository.hentPubliseringsdatoer()
        return publiseringsdatoerRepository
            .hentSisteImporttidspunkt()
            .map { forrigeImport: ImporttidspunktDto ->
                Publiseringsdatoer(
                    sistePubliseringsdato = forrigeImport.importertDato.toString(),
                    gjeldendePeriode = forrigeImport.gjeldendePeriode,
                    nestePubliseringsdato = finnNestePubliseringsdato(publiseringsdatoer, forrigeImport.importertDato)
                        .map { it.toString() }
                        .getOrElse("Neste publiseringsdato er utilgjengelig")
                )
            }
    }

    private fun finnNestePubliseringsdato(
        publiseringsdatoer: List<PubliseringsdatoDbDto>, forrigeImporttidspunkt: LocalDate
    ): Option<LocalDate> {
        val fremtidigePubliseringsdatoer = sorterEldsteDatoerFørst(
            filtrerBortDatoerEldreEnnForrigeLanseringsdato(
                publiseringsdatoer, forrigeImporttidspunkt
            )
        )
        return Try.of { fremtidigePubliseringsdatoer[0].offentligDato.toLocalDate() }
            .onFailure { log.warn("Ingen senere publiseringsdatoer er tilgjengelige i kalenderen") }
            .toOption()
    }

    private fun filtrerBortDatoerEldreEnnForrigeLanseringsdato(
        publiseringsdatoer: List<PubliseringsdatoDbDto>, forrigePubliseringsdato: LocalDate
    ): List<PubliseringsdatoDbDto> {
        return publiseringsdatoer.stream()
            .filter { (_, offentligDato): PubliseringsdatoDbDto ->
                offentligDato.toLocalDate().isAfter(forrigePubliseringsdato)
            }
            .collect(Collectors.toList())
    }

    companion object {
        private fun sorterEldsteDatoerFørst(
            datoer: List<PubliseringsdatoDbDto>
        ): List<PubliseringsdatoDbDto> {
            return datoer.stream()
                .sorted { obj: PubliseringsdatoDbDto, annen: PubliseringsdatoDbDto? ->
                    obj.sammenlignPubliseringsdatoer(
                        annen!!
                    )
                }
                .collect(Collectors.toList())
        }
    }
}
