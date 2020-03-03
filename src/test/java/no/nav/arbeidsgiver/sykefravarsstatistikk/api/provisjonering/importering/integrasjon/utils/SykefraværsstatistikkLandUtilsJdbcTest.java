package no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkLand;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkLand;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.DeleteSykefraværsstatistikkFunction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("db-test")
@RunWith(SpringRunner.class)
@DataJdbcTest
public class SykefraværsstatistikkLandUtilsJdbcTest {

    private static final String LABEL = "TEST";


    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private SykefraværsstatistikkLandUtils utils;

    @Before
    public void setUp() {
        utils = new SykefraværsstatistikkLandUtils(namedParameterJdbcTemplate);
        cleanUpLokalTestDb(namedParameterJdbcTemplate);
    }

    @After
    public void cleanUp() {
        cleanUpLokalTestDb(namedParameterJdbcTemplate);
    }


    @Test
    public void createFunction_apply__skal_lagre_data_i_lokale_sykefraværstatistikk_tabellen() {
        List<SykefraværsstatistikkLand> list = new ArrayList<>();
        list.add(
                new SykefraværsstatistikkLand(
                        2019,
                        1,
                        14,
                        new BigDecimal(55.123),
                        new BigDecimal(856.891)
                )
        );

        utils.getBatchCreateFunction(list).apply();

        List<Sykefraværprosent> resultList = hentSykefraværprosentLand(namedParameterJdbcTemplate);
        assertThat(resultList.size()).isEqualTo(1);
        assertThat(resultList.get(0)).isEqualTo((
                        new Sykefraværprosent(LABEL,
                                new BigDecimal(55.123),
                                new BigDecimal(856.891),
                                14
                        )
                )
        );
    }

    @Test
    public void deleteFunction_apply__skal_slette_data_i_lokale_sykefraværstatistikk_tabellen() {
        lagreSykefraværprosentLand(namedParameterJdbcTemplate, 2018, 3);
        lagreSykefraværprosentLand(namedParameterJdbcTemplate, 2018, 4);
        lagreSykefraværprosentLand(namedParameterJdbcTemplate, 2019, 1);

        DeleteSykefraværsstatistikkFunction deleteFunction = utils.getDeleteFunction();
        deleteFunction.apply(new ÅrstallOgKvartal(2018, 4));

        List<Sykefraværprosent> list = hentSykefraværprosentLand(namedParameterJdbcTemplate);
        assertThat(list.size()).isEqualTo(2);
    }


    private static List<Sykefraværprosent> hentSykefraværprosentLand(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {

        return namedParameterJdbcTemplate.query(
                "SELECT * FROM SYKEFRAVAR_STATISTIKK_LAND",
                new MapSqlParameterSource(),
                (rs, rowNum) -> new Sykefraværprosent(
                        LABEL,
                        rs.getBigDecimal("tapte_dagsverk"),
                        rs.getBigDecimal("mulige_dagsverk"),
                        rs.getInt("antall_personer")
                )
        );
    }

    private static void lagreSykefraværprosentLand(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            int årstall,
            int kvartal
    ) {
        namedParameterJdbcTemplate.update(
                String.format(
                        "insert into sykefravar_statistikk_land " +
                                "(arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) " +
                                "values (%d, %d, 15, 30, 300)",
                        årstall, kvartal
                ),
                new MapSqlParameterSource()
        );
    }

    private static void cleanUpLokalTestDb(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        namedParameterJdbcTemplate.update("DELETE FROM SYKEFRAVAR_STATISTIKK_LAND", new MapSqlParameterSource());
    }

}
