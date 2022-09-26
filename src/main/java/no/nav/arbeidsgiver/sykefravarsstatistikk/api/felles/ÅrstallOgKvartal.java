package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class ÅrstallOgKvartal implements Comparable<ÅrstallOgKvartal> {

    // TODO: Disse verdiene må oppdateres hver gang vi publiserer statistikk for nytt kvartal.
    // Det bør automatiseres bort på et vis.
    public static ÅrstallOgKvartal SISTE_PUBLISERTE_KVARTAL = new ÅrstallOgKvartal(2022, 2);

    private int årstall;
    private int kvartal;

    public static ÅrstallOgKvartal sisteKvartalMinus(int n) {
        return SISTE_PUBLISERTE_KVARTAL.minusKvartaler(n);
    }

    public ÅrstallOgKvartal(int årstall, int kvartal) {
        if (kvartal > 4 || kvartal < 1) {
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

    public ÅrstallOgKvartal minusEttÅr() {
        return new ÅrstallOgKvartal(this.årstall - 1, this.kvartal);
    }

    public static List<ÅrstallOgKvartal> sisteFireKvartaler(ÅrstallOgKvartal årstallOgKvartal) {
        return IntStream.range(0, 4)
                .mapToObj(årstallOgKvartal::minusKvartaler)
                .collect(Collectors.toList());
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

    @Override
    public String toString() {
        return kvartal + ". kvartal " + årstall;
    }
}
