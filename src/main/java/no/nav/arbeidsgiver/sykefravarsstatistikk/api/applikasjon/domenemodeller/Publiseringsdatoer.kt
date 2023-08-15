package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Data

@Data
@AllArgsConstructor
@Builder
class Publiseringsdatoer {
    private val sistePubliseringsdato: String? = null
    private val nestePubliseringsdato: String? = null
    private val gjeldendePeriode: ÅrstallOgKvartal? = null
}
