package no.nav.tag.sykefravarsstatistikk.api.controller;

import no.nav.tag.sykefravarsstatistikk.api.domain.stats.LandStatistikk;
import no.nav.tag.sykefravarsstatistikk.api.repository.LandStatistikkRepository;
import no.nav.tag.sykefravarsstatistikk.api.tilgangskontroll.TestController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
public class SykefravarsstatistikkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LandStatistikkRepository repository;

    // TODO Fjern
    @MockBean
    private TestController testController;

    @Test
    public void returnerer_list_av_landStatistikk() throws Exception {
        LandStatistikk sfProsent = LandStatistikk.builder()
                .arstall(2016)
                .kvartal(2)
                .muligeDagsverk(new BigDecimal(1000.55))
                .tapteDagsverk(new BigDecimal(56.699))
                .build();
        ArrayList<LandStatistikk> stats = new ArrayList<>();
        stats.add(sfProsent);
        when(repository.findAll()).thenReturn(stats);

        this.mockMvc.perform(get("/statistikk/land"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("[" +
                        "{\"arstall\":2016," +
                        "\"kvartal\":2," +
                        "\"mulige_dagsverk\":1000.55," +
                        "\"tapte_dagsverk\":56.699," +
                        "\"sykefravar_prosent\":5.7}]"));
    }


}