package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Publiseringsdatoer {
    private String gjeldendeÅrstall;
    private String gjeldendeKvartal;
    private String forrigePubliseringsdato;
    private String nestePubliseringsdato;
}