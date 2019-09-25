package no.nav.tag.sykefravarsstatistikk.api.sammenligning;

import no.nav.tag.sykefravarsstatistikk.api.sammenligning.SammenligningService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@ActiveProfiles("mvc-test")
@AutoConfigureMockMvc
@SpringBootTest
public class SammenligningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SammenligningService service;
/*
    @Test
    public void sykefravarprosent_krever_ikke_autentisering() throws Exception {
        LandStatistikk landStatistikk = LandStatistikk.builder()
                .arstall(2019)
                .kvartal(1)
                .muligeDagsverk(new BigDecimal(1000.55))
                .tapteDagsverk(new BigDecimal(56.699))
                .build();

        Sykefraværprosent2 sfProsent = Sykefraværprosent2.builder()
                .land(landStatistikk.beregnSykkefravarProsent())
                .build();

        when(service.hentSykefraværProsent()).thenReturn(sfProsent);

        this.mockMvc.perform(
                get("/sykefravarprosent"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{\"land\":5.7}")
                );
    }

    @Test
    public void sykefravarprosent_returnerer_404_dersom_ingen_stat_er_funnet() throws Exception {
        when(service.hentSykefraværProsent()).thenThrow(new ResourceNotFoundException("Har ikke funnet noe"));

        this.mockMvc.perform(
                get("/sykefravarprosent"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"message\":\"The resource is not found\"}"));
    }
 */

}