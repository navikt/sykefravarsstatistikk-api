package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.api;

import lombok.extern.slf4j.Slf4j;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene.OpprettEllerOppdaterResultat;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.importering.KlassifikasjonsimporteringService;
import no.nav.security.token.support.core.api.Protected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Protected
@RestController
@Slf4j
@RequestMapping(value = "provisjonering/synkronisering")
public class KlassifikasjonsimporteringController {

  private final KlassifikasjonsimporteringService service;

  @Autowired
  public KlassifikasjonsimporteringController(KlassifikasjonsimporteringService service) {
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
