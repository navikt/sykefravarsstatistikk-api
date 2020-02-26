package no.nav.tag.sykefravarsstatistikk.api.feratureToggles;

import no.nav.security.oidc.api.Unprotected;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Unprotected
@RestController
public class FeatureToggleController {
    private final FeatureToggleService featureToggleService;
    private final String UNLEASH_SESSION_COOKIE_NAME = "unleash-session";

    private static Logger logger = LoggerFactory.getLogger(ResponseEntityExceptionHandler.class);
    @Autowired
    public FeatureToggleController(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    @GetMapping("/feature")
    public ResponseEntity<Map<String, Boolean>> feature(
            @RequestParam("feature") List<String> features,
            @ApiIgnore @CookieValue(name = UNLEASH_SESSION_COOKIE_NAME, required = false) String unleashSession,
            HttpServletResponse response
    ) {
        String sessionId = unleashSession;

        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
            response.addCookie(new Cookie(UNLEASH_SESSION_COOKIE_NAME, sessionId));
        }

        Map<String, Boolean> toggles = featureToggleService.hentFeatureToggles(features, sessionId);
        return ResponseEntity.status(HttpStatus.OK).body(toggles);

    }

    @GetMapping("/testfeil")
    public ResponseEntity<Object> responseEntityFeilLogger(){
        logger.error("Uhåndtert feil for test error logg");
        return null;
    }

}
