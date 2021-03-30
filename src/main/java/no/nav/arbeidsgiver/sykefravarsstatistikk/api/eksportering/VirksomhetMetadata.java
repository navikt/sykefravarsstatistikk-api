package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;

public class VirksomhetMetadata {
    Orgnr orgnr;
    ÅrstallOgKvartal årstallOgKvartal;
    String sektor;
    String næring;
    String næringskode5Siffer;
    boolean erEksportert;

    public VirksomhetMetadata(
            Orgnr orgnr,
            ÅrstallOgKvartal årstallOgKvartal,
            String sektor,
            String næring,
            String næringskode5Siffer,
            boolean erEksportert
    ) {
        this.orgnr = orgnr;
        this.årstallOgKvartal = årstallOgKvartal;
        this.sektor = sektor;
        this.næring = næring;
        this.næringskode5Siffer = næringskode5Siffer;
        this.erEksportert = erEksportert;
    }


    public String getOrgnr() { return orgnr.getVerdi(); }
    public int getÅrstall() { return årstallOgKvartal.getÅrstall(); }
    public int getKvartal() { return årstallOgKvartal.getKvartal(); }
    public String getSektor() { return sektor; }
    public String getNæring() { return næring; }
    public String getNæringskode5Siffer() { return næringskode5Siffer; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VirksomhetMetadata that = (VirksomhetMetadata) o;

        if (erEksportert != that.erEksportert) return false;
        if (!orgnr.equals(that.orgnr)) return false;
        if (!årstallOgKvartal.equals(that.årstallOgKvartal)) return false;
        if (!sektor.equals(that.sektor)) return false;
        if (!næring.equals(that.næring)) return false;
        return næringskode5Siffer.equals(that.næringskode5Siffer);
    }

    @Override
    public int hashCode() {
        int result = orgnr.hashCode();
        result = 31 * result + årstallOgKvartal.hashCode();
        result = 31 * result + sektor.hashCode();
        result = 31 * result + næring.hashCode();
        result = 31 * result + næringskode5Siffer.hashCode();
        result = 31 * result + (erEksportert ? 1 : 0);
        return result;
    }
}
