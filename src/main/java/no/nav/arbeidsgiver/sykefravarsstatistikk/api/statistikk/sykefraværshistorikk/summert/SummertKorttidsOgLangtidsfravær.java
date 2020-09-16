package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SummertKorttidsOgLangtidsfravær {

    private SummertSykefravær korttidsfraværSiste4Kvartaler;
    private SummertSykefravær langtidsfraværSiste4Kvartaler;

}


