package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

public class MaskerteDataException extends StatistikkException {

    public MaskerteDataException() {
        super("Ikke nok personer i datagrunnlaget - data maskeres.");
    }
}
