package no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Unprotected;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Unprotected
@RestController
@Slf4j
@RequestMapping(value = "provisionering")
public class VirksomhetsklassifikasjonerSynkroniseringController {

    private final VirksomhetsklassifikasjonerSynkroniseringService service;

    @Autowired
    public VirksomhetsklassifikasjonerSynkroniseringController(VirksomhetsklassifikasjonerSynkroniseringService service){
        this.service = service;
    }

    @GetMapping(value = "/verif/datavarehus/sektorer")
    public List<Sektor> hentSektorer() {
        return service.hentSektorer();
    }

    @PostMapping(value = "/sektorer")
    public String populerSektorer() {
        service.populerSektorer();
        return "OK";
    }

}
