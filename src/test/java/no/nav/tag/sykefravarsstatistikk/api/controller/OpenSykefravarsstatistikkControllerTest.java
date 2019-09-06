package no.nav.tag.sykefravarsstatistikk.api.controller;

import no.nav.tag.sykefravarsstatistikk.api.domain.stats.LandStatistikk;
import no.nav.tag.sykefravarsstatistikk.api.repository.ResourceNotFoundException;
import no.nav.tag.sykefravarsstatistikk.api.sykefravarprosent.Sykefravarprosent;
import no.nav.tag.sykefravarsstatistikk.api.sykefravarprosent.SykefravarprosentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles("mvc-test")
@AutoConfigureMockMvc
@SpringBootTest
public class OpenSykefravarsstatistikkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SykefravarprosentService service;



    @Test
    public void sykefravarprosent_krever_ikke_autentisering() throws Exception {
        LandStatistikk landStatistikk = LandStatistikk.builder()
                .arstall(2019)
                .kvartal(1)
                .muligeDagsverk(new BigDecimal(1000.55))
                .tapteDagsverk(new BigDecimal(56.699))
                .build();

        Sykefravarprosent sfProsent = Sykefravarprosent.builder()
                .landStatistikk(landStatistikk)
                .build();

        when(service.hentSykefravarProsent()).thenReturn(sfProsent);

        this.mockMvc.perform(
                get("/sykefravarprosent"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"landStatistikk\":" +
                        "{\"arstall\":2019," +
                        "\"kvartal\":1," +
                        "\"mulige_dagsverk\":1000.55," +
                        "\"tapte_dagsverk\":56.699," +
                        "\"sykefravar_prosent\":5.7}}")
                );
    }

    @Test
    public void sykefravarprosent_returnerer_404_dersom_ingen_stat_er_funnet() throws Exception {
        when(service.hentSykefravarProsent()).thenThrow(new ResourceNotFoundException("Har ikke funnet noe"));

        this.mockMvc.perform(
                get("/sykefravarprosent"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"message\":\"The resource is not found\"}"));
    }

}