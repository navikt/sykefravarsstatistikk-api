package no.nav.tag.sykefravarsstatistikk.api.sykefravarprosenthistrorikk;

import no.nav.security.oidc.api.Protected;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Protected
@RestController
public class SykefraværprosentHistorikkController {
    private final SykefraværprosentHistorikkService sykefraværprosentHistorikkService;

    public SykefraværprosentHistorikkController(SykefraværprosentHistorikkService sykefraværprosentHistorikkService) {
        this.sykefraværprosentHistorikkService = sykefraværprosentHistorikkService;
    }

    @GetMapping(value = "/sykefravarprosenthistorikk/land")
    public KvartalsvisSykefraværprosentHistorikk hentStatistikkLand() {
        return sykefraværprosentHistorikkService.hentKvartalsvisSykefraværprosentHistorikkLand();
    }
}
