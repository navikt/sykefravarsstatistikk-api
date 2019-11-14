package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering;

import no.nav.tag.sykefravarsstatistikk.api.common.SlettOgOpprettResultat;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.CreateSykefraværsstatistikkFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.DeleteSykefraværsstatistikkFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils.SykefraværsstatistikkIntegrasjonUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class StatistikkImportRepositoryTest {

    @Mock
    NamedParameterJdbcTemplate jdbcTemplate;

    private StatistikkImportRepository statistikkImportRepository;


    @Before
    public void setUp() {
        statistikkImportRepository = new StatistikkImportRepository(jdbcTemplate);
    }

    @Test
    public void importStatistikk__skal_ikke_slette_eksisterende_statistikk_når_det_ikke_er_noe_data_å_importere() {

        SlettOgOpprettResultat resultat = statistikkImportRepository.importStatistikk(
                Collections.emptyList(),
                new ÅrstallOgKvartal(2019, 3),
                getIntegrasjonUtils()
        );

        assertEquals(resultat, SlettOgOpprettResultat.tomtResultat());
    }


    private static SykefraværsstatistikkIntegrasjonUtils getIntegrasjonUtils() {
        return new SykefraværsstatistikkIntegrasjonUtils() {
            @Override
            public DeleteSykefraværsstatistikkFunction getDeleteFunction() {
                return årstallOgKvartal -> {
                    fail("Skal ikke bruke delete funksjon");
                    return 0;
                };
            }

            @Override
            public CreateSykefraværsstatistikkFunction getCreateFunction() {
                return o -> {
                    fail("Skal ikke bruke create funksjon");
                    return 0;
                };
            }
        };
    }
}