package no.nav.tag.sykefravarsstatistikk.api.controller;

import no.nav.security.oidc.test.support.JwtTokenGenerator;
import no.nav.tag.sykefravarsstatistikk.api.domain.stats.LandStatistikk;
import no.nav.tag.sykefravarsstatistikk.api.repository.LandStatistikkRepository;
import org.junit.Before;
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
import java.util.ArrayList;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles("local")
@AutoConfigureMockMvc
@SpringBootTest
public class SykefravarsstatistikkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LandStatistikkRepository repository;

    private String jwt;
    private static String DUMMY_FNR = "01029900001";


    @Before
    public void setUp() {
        jwt = JwtTokenGenerator.createSignedJWT(DUMMY_FNR).serialize();
    }


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

        this.mockMvc.perform(get("/statistikk/land").header(AUTHORIZATION, "Bearer " + jwt))
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