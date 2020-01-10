package no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.sykefravarsstatistikk.api.common.OpprettEllerOppdaterResultat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Protected
@RestController
@Slf4j
@RequestMapping(value = "provisjonering/synkronisering")
public class VirksomhetsklassifikasjonerSynkroniseringController {

    private final VirksomhetsklassifikasjonerSynkroniseringService service;

    @Autowired
    public VirksomhetsklassifikasjonerSynkroniseringController(VirksomhetsklassifikasjonerSynkroniseringService service){
        this.service = service;
    }

    @PostMapping(value = "/sektorer")
    public OpprettEllerOppdaterResultat populerSektorer() {
        return service.populerSektorer();
    }

    @PostMapping(value = "/naringskoder")
    public OpprettEllerOppdaterResultat populerNæringskoder() {
        return service.populerNæringskoder();
    }

}
