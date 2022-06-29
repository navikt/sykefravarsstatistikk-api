package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;

public class VirksomhetEksportPerKvartal {
    private final Orgnr orgnr;
    private final ÅrstallOgKvartal årstallOgKvartal;
    private final boolean eksportert;


    public String getOrgnr() {
        return orgnr.getVerdi();
    }

    public int getÅrstall() {
        return årstallOgKvartal.getÅrstall();
    }

    public int getKvartal() {
        return årstallOgKvartal.getKvartal();
    }

    public ÅrstallOgKvartal getÅrstallOgKvartal() {
        return årstallOgKvartal;
    }

    public boolean eksportert() {
        return eksportert;
    }

    public VirksomhetEksportPerKvartal(Orgnr orgnr, ÅrstallOgKvartal årstallOgKvartal, boolean eksportert) {
        this.orgnr = orgnr;
        this.årstallOgKvartal = årstallOgKvartal;
        this.eksportert = eksportert;
    }
}
