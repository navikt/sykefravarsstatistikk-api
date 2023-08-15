package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import lombok.EqualsAndHashCode
import lombok.Value

/*
TODO Med importering av næringsgrupper vil importering av Næring-objektene være overflødig.
Da kan vi fjerne Virksomhetsklassifikasjon-interfacet.
Muligens kan vi fjerne hele Næring-klassen og bare bruke Næringsgruppering(?)
*/
@EqualsAndHashCode
@Value
class Næringsgruppering {
    private val kode5siffer: String? = null
    private val beskrivelse5siffer: String? = null
    private val kode4siffer: String? = null
    private val beskrivelse4siffer: String? = null
    private val kode3siffer: String? = null
    private val beskrivelse3siffer: String? = null
    private val kode2siffer: String? = null
    private val beskrivelse2siffer: String? = null
    private val kodeHovedområde: String? = null
    private val beskrivelseHovedområde: String? = null
}
