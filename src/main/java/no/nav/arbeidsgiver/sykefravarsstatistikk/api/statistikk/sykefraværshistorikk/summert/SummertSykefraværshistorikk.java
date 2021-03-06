package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import lombok.Builder;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SummertSykefravær;

@Data
@Builder
public class SummertSykefraværshistorikk {
    private Statistikkategori type;
    private String label;
    private SummertKorttidsOgLangtidsfravær summertKorttidsOgLangtidsfravær;
    private SummertSykefravær summertGradertFravær;
}
