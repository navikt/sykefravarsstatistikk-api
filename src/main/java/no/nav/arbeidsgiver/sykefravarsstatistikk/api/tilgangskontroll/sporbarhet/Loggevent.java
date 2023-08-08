package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.sporbarhet;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll.InnloggetBruker;

@AllArgsConstructor
@Data
public class Loggevent {
  private InnloggetBruker innloggetBruker;
  private Orgnr orgnr;
  private boolean harTilgang;
  private String requestMethod;
  private String requestUrl;
  private String altinnServiceCode;
  private String altinnServiceEdition;
}
