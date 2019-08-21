package no.nav.tag.sykefravarsstatistikk.api;

import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Import(TokenGeneratorConfiguration.class)
@Profile({"local"})
public class LocalOgUnitTestOidcConfiguration {
}
