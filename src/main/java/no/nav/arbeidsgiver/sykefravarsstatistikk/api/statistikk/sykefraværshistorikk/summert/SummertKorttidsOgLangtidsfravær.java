package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import lombok.AllArgsConstructor;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SummertSykefravær;

@Data
@AllArgsConstructor
public class SummertKorttidsOgLangtidsfravær {

    private SummertSykefravær summertKorttidsfravær;

    private SummertSykefravær summertLangtidsfravær;

}


