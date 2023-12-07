package config

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Profile

@Profile("local", "mvc-test")
@SpringBootApplication
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
@EnableConfigurationProperties
open class SykefraværsstatistikkLocalTestApplication
