package no.nav.tag.sykefravarsstatistikk.api.controller;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Protected;
import no.nav.tag.sykefravarsstatistikk.api.domain.Orgnr;
import no.nav.tag.sykefravarsstatistikk.api.domain.stats.LandStatistikk;
import no.nav.tag.sykefravarsstatistikk.api.repository.LandStatistikkRepository;
import no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll.TilgangskontrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Protected
@RestController
@Slf4j
public class SykefravarsstatistikkController {

    private final LandStatistikkRepository repository;
    private final TilgangskontrollService tilgangskontrollService;



    @Autowired
    public SykefravarsstatistikkController(LandStatistikkRepository repository, TilgangskontrollService tilgangskontrollService){
        this.repository = repository;
        this.tilgangskontrollService = tilgangskontrollService;
    }

    @GetMapping(value = "/status")
    public ResponseEntity status() {
            return ResponseEntity.status(HttpStatus.OK).body("RUNNING");
    }

    @GetMapping(value = "/statistikk/land")
    public Collection<LandStatistikk> landStatistikk() {
        try {
            Collection<LandStatistikk> landStatistikks = repository.findAll();
            return landStatistikks;
        } catch (Exception e) {
            log.error("Feil ved uthenting av landstatistikk", e);
            return null;
        }

    }

    @GetMapping(value = "/statistikk/land/ar/{arstall}")
    public Collection<LandStatistikk> landStatistikkForArstall(@PathVariable(required = false) int arstall) {
        try {
            Collection<LandStatistikk> landStatistikks = repository.findForArstall(arstall);
            return landStatistikks;
        } catch (Exception e) {
            log.error("Feil ved uthenting av landstatistikk", e);
            return null;
        }
    }

    @GetMapping(value = "/statistikk/bedrift/{orgnr}")
    public String statistikkForBedrift(@PathVariable String orgnr) {
        tilgangskontrollService.hentInnloggetBruker().sjekkTilgang(new Orgnr(orgnr));
        return "OK";
    }

}
