package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoinfo;

import lombok.Data;

@Data
public class PubliseringsdatoInfo {
    private String forrigePubliseringsDato;
    private String nestePubliseringsDato;
    private String gjeldendePeriode;
}