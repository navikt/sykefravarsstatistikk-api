package no.nav.tag.sykefravarsstatistikk.api.statistikk;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Unprotected;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.Sammenligning;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.Sykefraværprosent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Unprotected
@RestController
@Slf4j
public class OpenSykefraværsstatistikkController {

    private final SykefraværprosentService service;

    @Autowired
    public OpenSykefraværsstatistikkController(SykefraværprosentService service){
        this.service = service;
    }

    @Deprecated
    @GetMapping(value = "/sykefravarprosent")
    public Sykefraværprosent2 sykefraværprosent() {
        return service.hentSykefraværProsent();
    }

    @GetMapping(value = "/sammenligning")
    public Sammenligning sammenligning() {
        return service.hentSammenligning();
    }

}
