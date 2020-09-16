package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.SlettOgOpprettResultat;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.Sykefraværsstatistikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.importering.integrasjon.BatchCreateSykefraværsstatistikkFunction;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.importering.integrasjon.DeleteSykefraværsstatistikkFunction;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.importering.integrasjon.utils.SykefraværsstatistikkIntegrasjonUtils;
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
import java.util.stream.IntStream;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori._1_DAG_TIL_7_DAGER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class ImporteringRepositoryTest {

    @Mock
    NamedParameterJdbcTemplate jdbcTemplate;

    private ImporteringRepository importeringRepository;


    @Before
    public void setUp() {
        importeringRepository = new ImporteringRepository(jdbcTemplate);
    }

    @Test
    public void importStatistikk__skal_ikke_slette_eksisterende_statistikk_når_det_ikke_er_noe_data_å_importere() {

        SlettOgOpprettResultat resultat = importeringRepository.importStatistikk(
                "Test stats",
                Collections.emptyList(),
                new ÅrstallOgKvartal(2019, 3),
                getIntegrasjonUtils()
        );

        assertEquals(resultat, SlettOgOpprettResultat.tomtResultat());
    }

    @Test
    public void batchOpprett__deler_import_i_små_batch() {
        List<SykefraværsstatistikkVirksomhet> list = getSykefraværsstatistikkVirksomhetList(5);

        int resultat = importeringRepository.batchOpprett(
                list,
                dummyUtils(),
                2
        );

        assertEquals(5, resultat);
    }

    @Test
    public void batchOpprett__ikke_deler_dersom_batch_størrelse_er_større_enn_listen() {
        List<SykefraværsstatistikkVirksomhet> list = getSykefraværsstatistikkVirksomhetList(5);

        int resultat = importeringRepository.batchOpprett(
                list,
                dummyUtils(),
                1000
        );

        assertEquals(5, resultat);
    }


    private static List<SykefraværsstatistikkVirksomhet> getSykefraværsstatistikkVirksomhetList(int antallStatistikk) {
        List<SykefraværsstatistikkVirksomhet> list = new ArrayList<>();

        IntStream.range(0, antallStatistikk).forEach(
                i -> list.add(sykefraværsstatistikkVirksomhet((2000 + i), 1))
        );

        return list;
    }

    private static SykefraværsstatistikkVirksomhet sykefraværsstatistikkVirksomhet(int årstall, int kvartal) {
        return new SykefraværsstatistikkVirksomhet(
                årstall,
                kvartal,
                "987654321",
                _1_DAG_TIL_7_DAGER.kode,
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
            public BatchCreateSykefraværsstatistikkFunction getBatchCreateFunction(List<? extends Sykefraværsstatistikk> list) {
                return null;
            }
        };
    }

    private static SykefraværsstatistikkIntegrasjonUtils dummyUtils() {
        return new SykefraværsstatistikkIntegrasjonUtils() {
            @Override
            public DeleteSykefraværsstatistikkFunction getDeleteFunction() {
                return null;
            }

            @Override
            public BatchCreateSykefraværsstatistikkFunction getBatchCreateFunction(
                    List<? extends Sykefraværsstatistikk> list
            ) {
                return () -> list.size();
            }
        };
    }


}
