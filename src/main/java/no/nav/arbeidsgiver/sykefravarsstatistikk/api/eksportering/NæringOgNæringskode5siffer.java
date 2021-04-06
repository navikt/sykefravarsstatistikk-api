package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

public class NæringOgNæringskode5siffer {
    String næring;
    String næringskode5Siffer;

    public NæringOgNæringskode5siffer(
            String næring,
            String næringskode5Siffer
    ) {
        this.næring = næring;
        this.næringskode5Siffer = næringskode5Siffer;
    }


    public String getNæring() { return næring; }
    public String getNæringskode5Siffer() { return næringskode5Siffer; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NæringOgNæringskode5siffer that = (NæringOgNæringskode5siffer) o;

        if (!næring.equals(that.næring)) return false;
        return næringskode5Siffer.equals(that.næringskode5Siffer);
    }

    @Override
    public int hashCode() {
        int result = næring.hashCode();
        result = 31 * result + næringskode5Siffer.hashCode();
        return result;
    }
}
