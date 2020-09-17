package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SummertSykefravær;

@Data
@AllArgsConstructor
public class SummertKorttidsOgLangtidsfravær {

    private SummertSykefravær summertKorttidsfravær;

    private SummertSykefravær summertLangtidsfravær;

    // TODO Disse er bare her for bakoverkompatilibitet med frontend; fjern når frontend er oppdatert
    public SummertSykefravær getKorttidsfraværSiste4Kvartaler() {
        return summertKorttidsfravær;
    }

    public SummertSykefravær getLangtidsfraværSiste4Kvartaler() {
        return summertLangtidsfravær;
    }
}


