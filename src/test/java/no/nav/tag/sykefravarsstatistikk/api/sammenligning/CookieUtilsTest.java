package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.Cookie;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class CookieUtilsTest {
    @Test
    public void hentCookieEllerGenererNy__skal_håndtere_at_request_ikke_har_cookies() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(null);

        CookieUtils.hentCookieEllerGenererNy(request, new MockHttpServletResponse(), "DetteErMinCookie");
    }

    @Test
    public void hentCookieEllerGenererNy__skal_sette_cookie_hvis_den_ikke_finnes() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        CookieUtils.hentCookieEllerGenererNy(new MockHttpServletRequest(), response, "DetteErMinCookie");

        boolean cookieErSatt = Arrays.stream(response.getCookies()).anyMatch(cookie -> cookie.getName().equals("DetteErMinCookie"));
        assertThat(cookieErSatt).isTrue();
    }

    @Test
    public void hentCookieEllerGenererNy__skal_ikke_sette_ny_cookie_hvis_den_allerede_finnes() {
        String cookienavn = "DetteErMinCookie";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(cookienavn, "skal ikke overskrives"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        CookieUtils.hentCookieEllerGenererNy(request, response, cookienavn);

        List<Cookie> cookiesSomHarBlittSattPå = Arrays.stream(response.getCookies())
                .filter(cookie -> cookie.getName().equals(cookienavn))
                .collect(Collectors.toList());

        assertThat(cookiesSomHarBlittSattPå).isEmpty();
    }
}