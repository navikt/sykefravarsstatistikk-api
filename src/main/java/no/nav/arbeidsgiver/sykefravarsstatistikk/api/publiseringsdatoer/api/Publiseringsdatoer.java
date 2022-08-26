package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api;

import lombok.Data;

@Data
public class Publiseringsdatoer {
    private String forrigePubliseringsdato;
    private String nestePubliseringsdato;
    private String gjeldendePeriode;
}