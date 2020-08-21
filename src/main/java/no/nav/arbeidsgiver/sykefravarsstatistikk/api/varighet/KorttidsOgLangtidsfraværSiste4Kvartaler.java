package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KorttidsOgLangtidsfraværSiste4Kvartaler {

    private SykefraværSiste4Kvartaler korttidsfraværSiste4Kvartaler;
    private SykefraværSiste4Kvartaler langtidsfraværSiste4Kvartaler;

}


