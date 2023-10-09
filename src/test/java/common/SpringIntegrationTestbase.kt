package common

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.SykefraværsstatistikkLocalTestApplication
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

/**
 * Baseklasse for å initialisere Spring når en test kjører, slik at Spring ikke starter på nytt når
 * testen skal kjøres.
 */
@ActiveProfiles("mvc-test")
@EnableMockOAuth2Server
@SpringBootTest(
    classes = [SykefraværsstatistikkLocalTestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestPropertySource(properties = ["wiremock.mock.port=8082", "spring.h2.console.enabled=false", "management.endpoints.web.exposure.include=prometheus", "management.endpoints.web.base-path=/internal/actuator"])
@AutoConfigureMockMvc
@AutoConfigureObservability
open class SpringIntegrationTestbase
