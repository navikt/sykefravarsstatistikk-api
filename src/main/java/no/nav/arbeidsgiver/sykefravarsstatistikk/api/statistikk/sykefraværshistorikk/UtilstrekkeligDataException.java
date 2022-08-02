package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

public class UtilstrekkeligDataException extends StatistikkException {
    public UtilstrekkeligDataException(String message) {
        super("Mangler data for å gjennomføre kalkuleringen: " + message);
    }
}
