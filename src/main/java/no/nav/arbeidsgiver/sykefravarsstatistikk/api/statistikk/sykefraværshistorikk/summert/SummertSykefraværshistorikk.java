package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import com.fasterxml.jackson.annotation.JsonInclude;
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SummertSykefravær summertGradertFravær;
}
