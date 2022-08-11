package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram;

import java.util.Arrays;
import java.util.List;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;

@Data
public class Bransje {

    private final ArbeidsmiljøportalenBransje type;
    private final String navn;
    private final List<String> koderSomSpesifisererNæringer;

    public Bransje(
            ArbeidsmiljøportalenBransje type, String navn, String... koderSomSpesifisererNæringer) {
        this.type = type;
        this.navn = navn;
        this.koderSomSpesifisererNæringer = Arrays.asList(koderSomSpesifisererNæringer);
        validerKoder();
    }

    private void validerKoder() {
        if (inneholderKunKoderMedGittAntallSifre(2) || inneholderKunKoderMedGittAntallSifre(5)) {
            return;
        }
        throw new IllegalArgumentException(
                "Støtter kun bransjer som er spesifisert av enten 2 eller 5 sifre");
    }

    private boolean inneholderKunKoderMedGittAntallSifre(int antallSifre) {
        return koderSomSpesifisererNæringer.stream().allMatch(kode -> kode.length() == antallSifre);
    }

    public boolean erDefinertPåTosiffernivå() {
        return inneholderKunKoderMedGittAntallSifre(2);
    }

    public boolean erDefinertPåFemsiffernivå() {
        return inneholderKunKoderMedGittAntallSifre(5);
    }

    public boolean inkludererVirksomhet(Underenhet underenhet) {
        return inkludererNæringskode(underenhet.getNæringskode());
    }

    public boolean inkludererNæringskode(Næringskode5Siffer næringskode5Siffer) {
        String næringskode = næringskode5Siffer.getKode();
        return koderSomSpesifisererNæringer.stream().anyMatch(næringskode::startsWith);
    }
}
