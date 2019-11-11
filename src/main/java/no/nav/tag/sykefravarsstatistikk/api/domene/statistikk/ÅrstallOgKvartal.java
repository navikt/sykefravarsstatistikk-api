package no.nav.tag.sykefravarsstatistikk.api.domene.statistikk;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ÅrstallOgKvartal {
    private int årstall;
    private int kvartal;
}
