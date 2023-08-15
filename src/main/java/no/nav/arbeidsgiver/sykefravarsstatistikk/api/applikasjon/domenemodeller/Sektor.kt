package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import lombok.AllArgsConstructor
import lombok.Data

@Data
@AllArgsConstructor
class Sektor : Virksomhetsklassifikasjon {
    private override val kode: String? = null
    private override val navn: String? = null
}
