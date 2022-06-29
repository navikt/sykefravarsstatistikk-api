package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@EqualsAndHashCode
public class ÅrstallOgKvartal implements Comparable<ÅrstallOgKvartal> {
    private final int årstall;
    private final int kvartal;

    public ÅrstallOgKvartal(int årstall, int kvartal){
        if (kvartal > 4 || kvartal < 1){
            throw new IllegalArgumentException("Kvartal må være 1, 2, 3 eller 4");
        }
        this.årstall = årstall;
        this.kvartal = kvartal;
    }

    public ÅrstallOgKvartal minusKvartaler(int antallKvartaler) {
        if (antallKvartaler < 0) {
            return plussKvartaler(-antallKvartaler);
        }
        ÅrstallOgKvartal årstallOgKvartal = new ÅrstallOgKvartal(årstall, kvartal);
        for (int i = 0; i < antallKvartaler; i++) {
            årstallOgKvartal = årstallOgKvartal.forrigeKvartal();
        }
        return årstallOgKvartal;
    }

    public ÅrstallOgKvartal plussKvartaler(int antallKvartaler) {
        if (antallKvartaler < 0) {
            return minusKvartaler(-antallKvartaler);
        }
        ÅrstallOgKvartal årstallOgKvartal = new ÅrstallOgKvartal(årstall, kvartal);
        for (int i = 0; i < antallKvartaler; i++) {
            årstallOgKvartal = årstallOgKvartal.nesteKvartal();
        }
        return årstallOgKvartal;
    }

    public static List<ÅrstallOgKvartal> range(ÅrstallOgKvartal fra, ÅrstallOgKvartal til) {
        List<ÅrstallOgKvartal> årstallOgKvartal = new ArrayList<>();
        for (ÅrstallOgKvartal i = fra; i.compareTo(til) <= 0; i = i.plussKvartaler(1)) {
            årstallOgKvartal.add(i);
        }
        return årstallOgKvartal;
    }

    private ÅrstallOgKvartal forrigeKvartal() {
        if (kvartal == 1) {
            return new ÅrstallOgKvartal(årstall - 1, 4);
        } else {
            return new ÅrstallOgKvartal(årstall, kvartal - 1);
        }
    }

    private ÅrstallOgKvartal nesteKvartal() {
        if (kvartal == 4) {
            return new ÅrstallOgKvartal(årstall + 1, 1);
        } else {
            return new ÅrstallOgKvartal(årstall, kvartal + 1);
        }
    }

    @Override
    public int compareTo(@NotNull ÅrstallOgKvartal årstallOgKvartal) {
        return Comparator.comparing(ÅrstallOgKvartal::getÅrstall)
                .thenComparing(ÅrstallOgKvartal::getKvartal)
                .compare(this, årstallOgKvartal);
    }
}
