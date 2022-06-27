package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@EqualsAndHashCode
public class Kvartal implements Comparable<Kvartal> {
    private final int årstall;
    private final int kvartal;

    public Kvartal(int årstall, int kvartal){
        if (kvartal > 4 || kvartal < 1){
            throw new IllegalArgumentException("Kvartal må være 1, 2, 3 eller 4");
        }
        this.årstall = årstall;
        this.kvartal = kvartal;
    }

    public Kvartal minusKvartaler(int antallKvartaler) {
        if (antallKvartaler < 0) {
            return plussKvartaler(-antallKvartaler);
        }
        Kvartal kvartal = new Kvartal(årstall, this.kvartal);
        for (int i = 0; i < antallKvartaler; i++) {
            kvartal = kvartal.forrigeKvartal();
        }
        return kvartal;
    }

    public Kvartal plussKvartaler(int antallKvartaler) {
        if (antallKvartaler < 0) {
            return minusKvartaler(-antallKvartaler);
        }
        Kvartal kvartal = new Kvartal(årstall, this.kvartal);
        for (int i = 0; i < antallKvartaler; i++) {
            kvartal = kvartal.nesteKvartal();
        }
        return kvartal;
    }

    public static List<Kvartal> range(Kvartal fra, Kvartal til) {
        List<Kvartal> kvartal = new ArrayList<>();
        for (Kvartal i = fra; i.compareTo(til) <= 0; i = i.plussKvartaler(1)) {
            kvartal.add(i);
        }
        return kvartal;
    }

    private Kvartal forrigeKvartal() {
        if (kvartal == 1) {
            return new Kvartal(årstall - 1, 4);
        } else {
            return new Kvartal(årstall, kvartal - 1);
        }
    }

    private Kvartal nesteKvartal() {
        if (kvartal == 4) {
            return new Kvartal(årstall + 1, 1);
        } else {
            return new Kvartal(årstall, kvartal + 1);
        }
    }

    @Override
    public int compareTo(@NotNull Kvartal kvartal) {
        return Comparator.comparing(Kvartal::getÅrstall)
                .thenComparing(Kvartal::getKvartal)
                .compare(this, kvartal);
    }
}
