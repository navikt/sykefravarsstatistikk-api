package no.nav.arbeidsgiver.sykefravarsstatistikk.api.config;

import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Import(TokenGeneratorConfiguration.class)
@Profile({"local", "mvc-test"})
public class LocalOgUnitTestOidcConfiguration {
}
