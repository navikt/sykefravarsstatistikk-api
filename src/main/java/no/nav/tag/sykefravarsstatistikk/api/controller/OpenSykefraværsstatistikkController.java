package no.nav.tag.sykefravarsstatistikk.api.controller;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Unprotected;
import no.nav.tag.sykefravarsstatistikk.api.controller.json.SykefraværprosentJson;
import no.nav.tag.sykefravarsstatistikk.api.sykefravarprosent.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.sykefravarprosent.SykefraværprosentService;
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


    @GetMapping(value = "/sykefravarprosent")
    public SykefraværprosentJson sykefraværprosent() {
        return new SykefraværprosentJson(service.hentSykefraværProsent());
    }

}
