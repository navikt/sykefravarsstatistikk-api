package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering;

import no.nav.tag.sykefravarsstatistikk.api.common.SlettOgOpprettResultat;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkVirksomhet;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                "Test stats",
                Collections.emptyList(),
                new ÅrstallOgKvartal(2019, 3),
                getIntegrasjonUtils()
        );

        assertEquals(resultat, SlettOgOpprettResultat.tomtResultat());
    }

    @Test
    public void batchOpprett__deler_import_i_små_batch() {
        List<SykefraværsstatistikkVirksomhet> list = getSykefraværsstatistikkVirksomhetList();

        int resultat = statistikkImportRepository.batchOpprett(
                list,
                o -> 0,
                2
        );

        assertEquals(resultat, 5);
    }

    @Test
    public void batchOpprett__ikke_deler_dersom_batch_størrelse_er_større_enn_listen() {
        List<SykefraværsstatistikkVirksomhet> list = getSykefraværsstatistikkVirksomhetList();

        int resultat = statistikkImportRepository.batchOpprett(
                list,
                o -> 0,
                1000
        );

        assertEquals(resultat, 5);
    }


    private static List<SykefraværsstatistikkVirksomhet> getSykefraværsstatistikkVirksomhetList() {
        List<SykefraværsstatistikkVirksomhet> list = new ArrayList<>();
        list.add(sykefraværsstatistikkVirksomhet(2018, 1));
        list.add(sykefraværsstatistikkVirksomhet(2018, 2));
        list.add(sykefraværsstatistikkVirksomhet(2018, 3));
        list.add(sykefraværsstatistikkVirksomhet(2018, 4));
        list.add(sykefraværsstatistikkVirksomhet(2019, 1));
        return list;
    }

    private static SykefraværsstatistikkVirksomhet sykefraværsstatistikkVirksomhet(int årstall, int kvartal) {
        return new SykefraværsstatistikkVirksomhet(
                årstall,
                kvartal,
                "987654321",
                10,
                new BigDecimal(15),
                new BigDecimal(450)
        );
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