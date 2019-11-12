package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils;

import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkLand;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.CreateSykefraværsstatistikkFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.DeleteSykefraværsstatistikkFunction;
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
    public void skal_lagre_data_i_lokale_sykefraværstatistikk_tabellen(){
        CreateSykefraværsstatistikkFunction createFunction = utils.getCreateFunction();
        createFunction.apply(new SykefraværsstatistikkLand(2019, 1, 14, new BigDecimal(55.123), new BigDecimal(856.891)));

        List<Sykefraværprosent> list = hentSykefraværprosentLand(namedParameterJdbcTemplate);
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0)).isEqualTo((new Sykefraværprosent(LABEL, new BigDecimal(55.123), new BigDecimal(856.891))));
    }

    @Test
    public void skal_slette_data_i_lokale_sykefraværstatistikk_tabellen(){
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
                        rs.getBigDecimal("mulige_dagsverk")
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