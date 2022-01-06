package no.nav.arbeidsgiver.sykefravarsstatistikk.api;

import common.SpringIntegrationTestbase;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværshistorikkController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class SpringApplicationContextLoadTest extends SpringIntegrationTestbase {

    @Autowired
    private SykefraværshistorikkController controller;

    @Test
    public void contexLoads() {
        assertThat(controller).isNotNull();
    }

}
