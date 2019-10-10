package no.nav.tag.sykefravarsstatistikk.api.healthcheck;

import no.nav.tag.sykefravarsstatistikk.api.altinn.AltinnClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class HealthcheckControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    public void returnerer_OK() throws Exception {

        this.mockMvc.perform(get("/internal/healthcheck"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

}