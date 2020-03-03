package no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.importering.integrasjon;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;

@FunctionalInterface
public interface DeleteSykefraværsstatistikkFunction {
    int apply(ÅrstallOgKvartal årstallOgKvartal);
}
