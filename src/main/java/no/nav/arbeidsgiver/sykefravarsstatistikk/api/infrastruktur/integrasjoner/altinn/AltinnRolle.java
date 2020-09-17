package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.integrasjoner.altinn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AltinnRolle {
    @JsonProperty("RoleType")
    private String type;
    @JsonProperty("RoleDefinitionId")
    private String definitionId;
    @JsonProperty("RoleName")
    private String name;
    @JsonProperty("RoleDescription")
    private String description;
}
