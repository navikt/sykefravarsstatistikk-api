package no.nav.tag.sykefravarsstatistikk.api.featureToggles;

import no.nav.tag.sykefravarsstatistikk.api.feratureToggles.FeatureToggleController;
import no.nav.tag.sykefravarsstatistikk.api.feratureToggles.FeatureToggleService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FeatureToggleControllerTest {

    @Mock
    HttpServletResponse response;
    @Mock
    FeatureToggleService featureToggleService;

    private FeatureToggleController featureToggleController;

    @Before
    public void setup() {
        featureToggleController = new FeatureToggleController(featureToggleService);
    }

    @Test
    public void feature__skal_sette_cookie_hvis_ingen_cookie() {
        featureToggleController.feature(null, null, response);
        verify(response).addCookie(any());
    }

    @Test
    public void feature__skal_ikke_sette_cookie_hvis_man_har_cookie() {
        featureToggleController.feature(null, "blabla", response);
        verify(response, times(0)).addCookie(any());
    }

    @Test
    public void feature__skal_returnere_status_200_ved_get() {
        assertThat(featureToggleController.feature(Arrays.asList("darkMode", "nightMode"), null, response).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void feature__skal_returnere_respons_fra_service() {
        List<String> features = Arrays.asList("darkMode", "nightMode");
        Map<String, Boolean> toggles = new HashMap<>() {{
            put("darkMode", true);
            put("nightMode", false);
        }};

        when(featureToggleService.hentFeatureToggles(eq(features), any())).thenReturn(toggles);

        Map<String, Boolean> resultat = featureToggleController.feature(features, null, response).getBody();

        assertThat(resultat).isEqualTo(toggles);
    }
}
