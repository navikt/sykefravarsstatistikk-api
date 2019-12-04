package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.html.Option;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class CookieUtils {
    public static String hentCookieEllerGenererNy(
            HttpServletRequest request,
            HttpServletResponse response,
            String cookieName
    ) {
        return settPåCookieOgReturnerVerdi(request, response, cookieName);
    }

    private static String settPåCookieOgReturnerVerdi(HttpServletRequest request, HttpServletResponse response, String cookieName) {
        Optional<Cookie> cookie = getStatistikkIdCookie(request, cookieName);

        if (cookie.isPresent()) {
            return cookie.get().getValue();
        } else {
            return setStatistikkIdCookie(response, cookieName);
        }
    }

    private static Optional<Cookie> getStatistikkIdCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .findAny();
    }

    private static String setStatistikkIdCookie(HttpServletResponse response, String cookieName) {
        String statistikkId = UUID.randomUUID().toString();
        response.addCookie(new Cookie(cookieName, statistikkId));
        return statistikkId;
    }

}
