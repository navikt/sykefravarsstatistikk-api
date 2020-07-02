package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableJwtTokenValidation(ignore = {
        "springfox.documentation.swagger.web.ApiResourceController",
        "org.springframework"
})
public class SykefraværsstatistikkApplication {

    public static void main(String[] args) {
        SpringApplication.run(SykefraværsstatistikkApplication.class, args);
    }
}
