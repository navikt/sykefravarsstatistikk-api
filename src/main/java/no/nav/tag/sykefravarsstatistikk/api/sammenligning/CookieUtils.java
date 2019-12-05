package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        Optional<Cookie> cookie = getCookie(request, cookieName);

        if (cookie.isPresent()) {
            return cookie.get().getValue();
        } else {
            return settCookieOgReturnerVerdi(response, cookieName);
        }
    }

    private static Optional<Cookie> getCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .findAny();
    }

    private static String settCookieOgReturnerVerdi(HttpServletResponse response, String cookieName) {
        String statistikkId = UUID.randomUUID().toString();
        response.addCookie(new Cookie(cookieName, statistikkId));
        return statistikkId;
    }

}
