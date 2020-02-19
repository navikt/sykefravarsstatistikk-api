package no.nav.tag.sykefravarsstatistikk.api;

import no.nav.security.spring.oidc.api.EnableOIDCTokenValidation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableOIDCTokenValidation(ignore = {
        "springfox.documentation.swagger.web.ApiResourceController",
        "org.springframework"
})
public class SykefraværsstatistikkApplication {

    public static void main(String[] args) {
        SpringApplication.run(SykefraværsstatistikkApplication.class, args);
    }
}
