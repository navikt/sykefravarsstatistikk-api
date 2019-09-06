package no.nav.tag.sykefravarsstatistikk.api.controller;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Unprotected;
import no.nav.tag.sykefravarsstatistikk.api.sykefravarprosent.Sykefravarprosent;
import no.nav.tag.sykefravarsstatistikk.api.sykefravarprosent.SykefravarprosentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Unprotected
@RestController
@Slf4j
public class OpenSykefravarsstatistikkController {

    private final SykefravarprosentService service;


    @Autowired
    public OpenSykefravarsstatistikkController(SykefravarprosentService service){
        this.service = service;
    }


    @GetMapping(value = "/sykefravarprosent")
    public Sykefravarprosent sykefravarprosent() {
        Sykefravarprosent sykefravarprosent = service.hentSykefravarProsent();
        return sykefravarprosent;
    }

}
