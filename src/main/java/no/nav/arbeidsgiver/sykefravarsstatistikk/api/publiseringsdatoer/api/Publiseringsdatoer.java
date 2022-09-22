package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer.api;

import lombok.Builder;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;

@Data
@Builder
public class Publiseringsdatoer {
    private String sistePubliseringsdato;
    private String nestePubliseringsdato;
    private ÅrstallOgKvartal gjeldendePeriode;
}