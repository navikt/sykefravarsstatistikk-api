package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.featuretoggling;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.unleash.UnleashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FeatureToggleControllerTest {

    @Mock
    HttpServletResponse response;
    @Mock
    UnleashService unleashService;

    private FeatureToggleController featureToggleController;

    @BeforeEach
    public void setup() {
        featureToggleController = new FeatureToggleController(unleashService);
    }

    @Test
    public void feature__skal_sette_cookie_hvis_ingen_cookie() {
        featureToggleController.feature(null, null, response);
        Cookie unleashCookie = new Cookie(featureToggleController.UNLEASH_SESSION_COOKIE_NAME, "unleashSession");
        unleashCookie.setSecure(true);

        ArgumentCaptor<Cookie> argumentCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(argumentCaptor.capture());
        Cookie capturedArgument = argumentCaptor.getValue();

        assertThat(capturedArgument.getName()).isEqualTo(featureToggleController.UNLEASH_SESSION_COOKIE_NAME);
        assertThat(capturedArgument.getSecure()).isTrue();
    }

    @Test
    public void feature__skal_ikke_sette_cookie_hvis_man_har_cookie() {
        featureToggleController.feature(null, "blabla", response);
        verify(response, times(0)).addCookie(any());
    }

    @Test
    public void feature__skal_returnere_status_200_ved_get() {
        assertThat(
                featureToggleController.feature(
                        Arrays.asList("darkMode", "nightMode"), null, response).getStatusCode()
        ).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void feature__skal_returnere_respons_fra_service() {
        List<String> features = Arrays.asList("darkMode", "nightMode");
        Map<String, Boolean> toggles = new HashMap<>() {{
            put("darkMode", true);
            put("nightMode", false);
        }};

        when(unleashService.hentFeatureToggles(eq(features), any())).thenReturn(toggles);

        Map<String, Boolean> resultat = featureToggleController.feature(features, null, response).getBody();

        assertThat(resultat).isEqualTo(toggles);
    }
}
