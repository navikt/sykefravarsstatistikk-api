package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.exceptions;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.exceptions.StatistikkException;

public class UtilstrekkeligDataException extends StatistikkException {
  public UtilstrekkeligDataException(String message) {
    super("Mangler data for å gjennomføre kalkuleringen: " + message);
  }

  public UtilstrekkeligDataException() {
    this("ingen sykefraværsdata ble funnet.");
  }
}
