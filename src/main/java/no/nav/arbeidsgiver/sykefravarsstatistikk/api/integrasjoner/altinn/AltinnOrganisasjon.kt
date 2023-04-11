package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.altinn

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Data

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
data class AltinnOrganisasjon(
    @field:JsonProperty("Name") val name: String?,
    @field:JsonProperty("Type") val type: String?,
    @field:JsonProperty("ParentOrganizationNumber") val parentOrganizationNumber: String?,
    @field:JsonProperty("OrganizationNumber") val organizationNumber: String?,
    @field:JsonProperty("OrganizationForm") val organizationForm: String?,
    @field:JsonProperty("Status") val status: String?,
)