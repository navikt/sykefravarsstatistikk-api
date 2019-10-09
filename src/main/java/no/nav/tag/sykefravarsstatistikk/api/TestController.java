package no.nav.tag.sykefravarsstatistikk.api;

import lombok.extern.slf4j.Slf4j;
import no.nav.security.oidc.api.Unprotected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Unprotected
public class TestController {

    private Logger auditLogger = LoggerFactory.getLogger("auditLogger");

    @GetMapping(value = "/test")
    public String test() {
        auditLogger.info("tester auditlogging");
        log.info("tester auditlogging. dette er ikke auditlogging.");
        return "auditlog OK";
    }
}
