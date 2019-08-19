package no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

//TODO Fjern denne klassen

@RestController
public class TestController {
    private final TilgangskontrollService tilgangskontrollService;

    public TestController(TilgangskontrollService tilgangskontrollService) {
        this.tilgangskontrollService = tilgangskontrollService;
    }

    @GetMapping(value = "/test/{fnr}/{orgnr}")
    public String healthcheck(
            @PathVariable String fnr,
            @PathVariable String orgnr
    ) {
        tilgangskontrollService.sjekkTilgang(fnr, orgnr);
        return "ok";
    }
}
