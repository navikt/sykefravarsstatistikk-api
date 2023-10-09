package no.nav.arbeidsgiver.sykefravarsstatistikk.api

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableJwtTokenValidation(ignore = ["org.springdoc", "org.springframework"])
open class SykefraværsstatistikkApplication

fun main(args: Array<String>) {
    runApplication<SykefraværsstatistikkApplication>(*args)
}
