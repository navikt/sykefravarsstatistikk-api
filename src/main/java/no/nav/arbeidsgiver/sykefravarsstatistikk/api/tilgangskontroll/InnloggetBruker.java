package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Fnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.altinn.AltinnOrganisasjon;

@Data
public class InnloggetBruker {

  private List<AltinnOrganisasjon> brukerensOrganisasjoner;
  private Fnr fnr;

  public InnloggetBruker(Fnr fnr) {
    this.fnr = fnr;
    brukerensOrganisasjoner = new ArrayList<>();
  }

  public void sjekkTilgang(Orgnr orgnr) {
    if (!harTilgang(orgnr)) {
      throw new TilgangskontrollException("Har ikke tilgang til statistikk for denne bedriften.");
    }
  }

  public boolean harTilgang(Orgnr orgnr) {
    List<String> orgnumreBrukerHarTilgangTil =
        brukerensOrganisasjoner.stream()
            .filter(Objects::nonNull)
            .map(AltinnOrganisasjon::getOrganizationNumber)
            .collect(Collectors.toList());

    return orgnumreBrukerHarTilgangTil.contains(orgnr.getVerdi());
  }
}
