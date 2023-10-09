package no.nav.arbeidsgiver.sykefravarsstatistikk.api

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Profile

@Profile("local", "mvc-test")
@SpringBootApplication
@ComponentScan(
    basePackages = ["no.nav.arbeidsgiver"],
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        value = [AppConfigForJdbcTesterConfig::class]
    )]
)
@EnableJwtTokenValidation(ignore = ["org.springframework", "org.springdoc"])
@EnableConfigurationProperties
open class SykefraværsstatistikkLocalTestApplication
