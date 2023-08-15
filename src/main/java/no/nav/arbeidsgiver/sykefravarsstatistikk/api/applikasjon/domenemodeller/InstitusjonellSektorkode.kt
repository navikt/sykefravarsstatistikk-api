package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Value

@Value
class InstitusjonellSektorkode @JsonCreator constructor(
    @param:JsonProperty("kode") private val kode: String,
    @param:JsonProperty("beskrivelse") private val beskrivelse: String
)
