package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

public class ManglendeDataException extends RuntimeException{
    ManglendeDataException(String message) {
        super("Mangler data for å gjennomføre kalkuleringen: " + message);
    }
}
