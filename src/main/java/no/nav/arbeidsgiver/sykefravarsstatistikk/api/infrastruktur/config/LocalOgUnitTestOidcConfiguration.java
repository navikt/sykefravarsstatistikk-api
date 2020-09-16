package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config;

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Configuration
@Import(TokenGeneratorConfiguration.class)
@Profile({"local", "mvc-test"})
public class LocalOgUnitTestOidcConfiguration {
}
