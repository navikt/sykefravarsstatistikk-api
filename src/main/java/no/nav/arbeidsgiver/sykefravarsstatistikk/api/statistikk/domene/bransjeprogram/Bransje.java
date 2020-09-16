package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.bransjeprogram;

import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.enhetsregisteret.Underenhet;

import java.util.Arrays;
import java.util.List;

@Data
public class Bransje {
    private final Bransjetype type;
    private final String navn;
    private final List<String> koderSomSpesifisererNæringer;

    public Bransje(Bransjetype type, String navn, String... koderSomSpesifisererNæringer) {
        this.type = type;
        this.navn = navn;
        this.koderSomSpesifisererNæringer = Arrays.asList(koderSomSpesifisererNæringer);
        validerKoder();
    }

    private void validerKoder() {
        if (inneholderKunKoderMedGittAntallSifre(2) || inneholderKunKoderMedGittAntallSifre(5)) {
            return;
        }
        throw new IllegalArgumentException("Støtter kun bransjer som er spesifisert av enten 2 eller 5 sifre");
    }

    private boolean inneholderKunKoderMedGittAntallSifre(int antallSifre) {
        return koderSomSpesifisererNæringer.stream().allMatch(kode -> kode.length() == antallSifre);
    }

    public int lengdePåNæringskoder() {
        if (inneholderKunKoderMedGittAntallSifre(2)) {
            return 2;
        } else if (inneholderKunKoderMedGittAntallSifre(5)) {
            return 5;
        }
        throw new IllegalStateException("Støtter kun bransjer som er spesifisert av enten 2 eller 5 sifre");
    }

    public boolean inkludererVirksomhet(Underenhet underenhet) {
        String næringskode = underenhet.getNæringskode().getKode();
        return koderSomSpesifisererNæringer.stream().anyMatch(næringskode::startsWith);
    }
}
