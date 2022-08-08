package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

public class MaskerteDataException extends StatistikkException {

    public MaskerteDataException(String årsakTilMaskering) {
        super("Data er maskert: " + årsakTilMaskering);
    }
}
