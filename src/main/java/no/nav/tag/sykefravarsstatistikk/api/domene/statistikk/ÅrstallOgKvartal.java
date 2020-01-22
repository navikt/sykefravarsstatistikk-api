package no.nav.tag.sykefravarsstatistikk.api.domene.statistikk;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ÅrstallOgKvartal {
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
}
