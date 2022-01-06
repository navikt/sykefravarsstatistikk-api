package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tilgangskontroll;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenXClientTest {

    @Test
    public void wellKnownUrl_skal_trimmes_slik_at_resolveUrl_fungerer() {
        assertThat(
                TokenXClient.trimUrl("https://tokendings.dev-gcp.nais.io/.well-known/oauth-authorization-server/")
        ).isEqualTo("https://tokendings.dev-gcp.nais.io/");

        assertThat(
                TokenXClient.trimUrl("https://tokendings.dev-gcp.nais.io/.well-known/oauth-authorization-server")
        ).isEqualTo("https://tokendings.dev-gcp.nais.io");

    }

}