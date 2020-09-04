package no.nav.arbeidsgiver.sykefravarsstatistikk.api.config;

import org.apache.tomcat.util.http.LegacyCookieProcessor;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Tomcat logger feilformaterte cookies by default. Dette er et sikkerhetshull.
// Her fikser vi dette ved å bruke LegacyCookieProcessor, som ikke logger slike.
@Configuration
public class TomcatConfig {
    @Bean
    WebServerFactoryCustomizer<TomcatServletWebServerFactory> cookieProcessorCustomizer() {
        return new WebServerFactoryCustomizer<>() {
            @Override
            public void customize(TomcatServletWebServerFactory tomcatServletWebServerFactory) {
                tomcatServletWebServerFactory
                        .addContextCustomizers(context -> context.setCookieProcessor(new LegacyCookieProcessor()));
            }
        };
    }
}
