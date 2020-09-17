package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.featuretoggling;

import io.swagger.v3.oas.annotations.Parameter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.unleash.UnleashService;
import no.nav.security.token.support.core.api.Unprotected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Unprotected
@RestController
public class FeatureToggleController {
    private final UnleashService unleashService;
    private final String UNLEASH_SESSION_COOKIE_NAME = "unleash-session";


    @Autowired
    public FeatureToggleController(UnleashService unleashService) {
        this.unleashService = unleashService;
    }

    @GetMapping("/feature")
    public ResponseEntity<Map<String, Boolean>> feature(
            @RequestParam("feature") List<String> features,
            @Parameter(hidden = true) @CookieValue(name = UNLEASH_SESSION_COOKIE_NAME, required = false) String unleashSession,
            HttpServletResponse response
    ) {
        String sessionId = unleashSession;

        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
            response.addCookie(new Cookie(UNLEASH_SESSION_COOKIE_NAME, sessionId));
        }

        Map<String, Boolean> toggles = unleashService.hentFeatureToggles(features, sessionId);
        return ResponseEntity.status(HttpStatus.OK).body(toggles);

    }

}
