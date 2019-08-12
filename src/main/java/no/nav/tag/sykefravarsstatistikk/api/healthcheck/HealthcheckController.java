package no.nav.tag.sykefravarsstatistikk.api.healthcheck;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthcheckController {


    @GetMapping(value = "/internal/healthcheck")
    public String healthcheck() {
        return "ok";
    }
}
