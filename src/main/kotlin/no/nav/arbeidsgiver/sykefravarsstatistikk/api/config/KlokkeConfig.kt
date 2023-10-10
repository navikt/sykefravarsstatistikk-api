package no.nav.arbeidsgiver.sykefravarsstatistikk.api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
open class KlokkeConfig {
    @Bean
    open fun klokke(): Clock = Clock.systemDefaultZone()
}