package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles;

public class Konstanter {
    public static final ÅrstallOgKvartal SISTE_PUBLISERTE_KVARTAL = new ÅrstallOgKvartal(2022, 1);
public static int antallKvartalerSomSkalSummeres = 4;

    public static final int MINIMUM_ANTALL_PERSONER_SOM_SKAL_TIL_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER = 5;

    public static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-ID";
    public static final String CONSUMER_ID_HEADER_NAME = "X-Consumer-ID";
}
