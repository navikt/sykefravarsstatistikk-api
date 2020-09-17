package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.klassifikasjoner;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.OpprettEllerOppdaterResultat;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Protected
@RestController
@Slf4j
@RequestMapping(value = "provisjonering/synkronisering")
public class VirksomhetsklassifikasjonerSynkroniseringController {

    private final KlassifikasjonerService service;

    @Autowired
    public VirksomhetsklassifikasjonerSynkroniseringController(KlassifikasjonerService service) {
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
