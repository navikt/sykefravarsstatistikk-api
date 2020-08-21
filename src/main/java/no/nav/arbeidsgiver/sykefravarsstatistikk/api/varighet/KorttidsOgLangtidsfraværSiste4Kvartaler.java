package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import lombok.Data;

@Data
public class KorttidsOgLangtidsfraværSiste4Kvartaler {

    private KorttidsEllerLangtidsfraværSiste4Kvartaler korttidsfraværSiste4Kvartaler;
    private KorttidsEllerLangtidsfraværSiste4Kvartaler langtidsfraværSiste4Kvartaler;

}


