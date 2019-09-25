package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Unprotected;
import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sammenligning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Unprotected
@RestController
@Slf4j
public class SammenligningController {

    private final SammenligningService service;

    @Autowired
    public SammenligningController(SammenligningService service){
        this.service = service;
    }

    @GetMapping(value = "/sammenligning")
    public Sammenligning sammenligning() {
        return service.hentSammenligning();
    }

}
