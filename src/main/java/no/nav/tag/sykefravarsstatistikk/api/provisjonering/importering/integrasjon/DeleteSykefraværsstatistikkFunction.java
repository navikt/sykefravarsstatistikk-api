package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon;

import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;

@FunctionalInterface
public interface DeleteSykefraværsstatistikkFunction {
    int apply(ÅrstallOgKvartal årstallOgKvartal);
}
