package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.KafkaProperties;
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableJwtTokenValidation(ignore = {
        "org.springdoc",
        "org.springframework"
})
@EnableConfigurationProperties(value = KafkaProperties.class)
public class SykefraværsstatistikkApplication {
    public static void main(String[] args) {
        SpringApplication.run(SykefraværsstatistikkApplication.class, args);
    }
}
