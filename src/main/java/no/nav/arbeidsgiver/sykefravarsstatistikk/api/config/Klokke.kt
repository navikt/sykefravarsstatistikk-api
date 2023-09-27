package no.nav.arbeidsgiver.sykefravarsstatistikk.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.Clock

@Configuration
open class Klokke {
    @Primary
    @Bean("klokke")
    open fun klokke(): Clock = Clock.systemDefaultZone()
}