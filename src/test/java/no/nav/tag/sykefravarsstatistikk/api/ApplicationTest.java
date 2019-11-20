package no.nav.tag.sykefravarsstatistikk.api;

import no.nav.tag.sykefravarsstatistikk.api.sammenligning.SammenligningController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = {"wiremock.mock.port=8082", "spring.h2.console.enabled=false"})
public class ApplicationTest {

    @Autowired
    private SammenligningController controller;

    @Test
    public void contexLoads() {
        assertThat(controller).isNotNull();
    }

}
