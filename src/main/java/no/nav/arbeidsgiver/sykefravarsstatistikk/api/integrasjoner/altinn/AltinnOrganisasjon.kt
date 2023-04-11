package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.altinn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AltinnOrganisasjon {
  @JsonProperty("Name")
  private String name;

  @JsonProperty("Type")
  private String type;

  @JsonProperty("ParentOrganizationNumber")
  private String parentOrganizationNumber;

  @JsonProperty("OrganizationNumber")
  private String organizationNumber;

  @JsonProperty("OrganizationForm")
  private String organizationForm;

  @JsonProperty("Status")
  private String status;
}
