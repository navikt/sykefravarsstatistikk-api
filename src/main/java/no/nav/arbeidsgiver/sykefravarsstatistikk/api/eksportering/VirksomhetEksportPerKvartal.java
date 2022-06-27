package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Kvartal;

public class VirksomhetEksportPerKvartal {
    private final Orgnr orgnr;
    private final Kvartal kvartal;
    private final boolean eksportert;


    public String getOrgnr() {
        return orgnr.getVerdi();
    }

    public int getÅrstall() {
        return kvartal.getÅrstall();
    }

    public int getKvartal() {
        return kvartal.getKvartal();
    }

    public Kvartal getÅrstallOgKvartal() {
        return kvartal;
    }

    public boolean eksportert() {
        return eksportert;
    }

    public VirksomhetEksportPerKvartal(Orgnr orgnr, Kvartal kvartal, boolean eksportert) {
        this.orgnr = orgnr;
        this.kvartal = kvartal;
        this.eksportert = eksportert;
    }
}
