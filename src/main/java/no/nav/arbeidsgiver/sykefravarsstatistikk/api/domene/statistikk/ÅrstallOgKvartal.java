package no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Comparator;

@Data
@AllArgsConstructor
public class ÅrstallOgKvartal implements Comparable<ÅrstallOgKvartal> {
    private final int årstall;
    private final int kvartal;


    public ÅrstallOgKvartal minusKvartaler(int antallKvartaler) {
        ÅrstallOgKvartal årstallOgKvartal = new ÅrstallOgKvartal(årstall, kvartal);
        for (int i = 0; i < antallKvartaler; i++) {
            årstallOgKvartal = årstallOgKvartal.hentForrigeKvartal();
        }
        return årstallOgKvartal;
    }

    private ÅrstallOgKvartal hentForrigeKvartal() {
        if (kvartal == 1) {
            return new ÅrstallOgKvartal(årstall - 1, 4);
        } else {
            return new ÅrstallOgKvartal(årstall, kvartal - 1);
        }
    }

    @Override
    public int compareTo(ÅrstallOgKvartal årstallOgKvartal) {
        return Comparator.comparing(ÅrstallOgKvartal::getÅrstall)
                .thenComparing(ÅrstallOgKvartal::getKvartal)
                .compare(this, årstallOgKvartal);
    }
}
