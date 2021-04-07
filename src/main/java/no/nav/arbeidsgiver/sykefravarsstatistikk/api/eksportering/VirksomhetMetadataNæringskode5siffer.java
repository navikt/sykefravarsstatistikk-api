package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;

public class VirksomhetMetadataNæringskode5siffer {
    public VirksomhetMetadataNæringskode5siffer(Orgnr orgnr, ÅrstallOgKvartal årstallOgKvartal, NæringOgNæringskode5siffer næringOgNæringskode5siffer) {
        this.orgnr = orgnr;
        this.årstallOgKvartal = årstallOgKvartal;
        this.næringOgNæringskode5siffer = næringOgNæringskode5siffer;
    }

    private final Orgnr orgnr;
    private final ÅrstallOgKvartal årstallOgKvartal;
    private final NæringOgNæringskode5siffer næringOgNæringskode5siffer;

    public String getOrgnr() { return orgnr.getVerdi(); }
    public int getÅrstall() { return årstallOgKvartal.getÅrstall(); }
    public int getKvartal() { return årstallOgKvartal.getKvartal(); }
    public String getNæring() { return næringOgNæringskode5siffer.getNæring(); }
    public String getNæringskode5siffer() { return næringOgNæringskode5siffer.getNæringskode5Siffer(); }
}
