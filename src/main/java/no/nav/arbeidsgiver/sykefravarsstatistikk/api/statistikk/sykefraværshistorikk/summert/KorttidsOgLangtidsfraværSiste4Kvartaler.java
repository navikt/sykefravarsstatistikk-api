package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.sykefravær.SykefraværSiste4Kvartaler;

@Data
@AllArgsConstructor
public class KorttidsOgLangtidsfraværSiste4Kvartaler {

    private SykefraværSiste4Kvartaler korttidsfraværSiste4Kvartaler;
    private SykefraværSiste4Kvartaler langtidsfraværSiste4Kvartaler;

}


