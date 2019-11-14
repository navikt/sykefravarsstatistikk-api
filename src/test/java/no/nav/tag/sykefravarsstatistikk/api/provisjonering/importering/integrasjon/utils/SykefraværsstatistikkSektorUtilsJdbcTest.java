package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils;

import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkSektor;
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
public class SykefraværsstatistikkSektorUtilsJdbcTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private SykefraværsstatistikkSektorUtils utils;

    private static final String LABEL = "TEST";


    @Before
    public void setUp() {
        utils = new SykefraværsstatistikkSektorUtils(namedParameterJdbcTemplate);
        cleanUpLokalTestDb(namedParameterJdbcTemplate);
    }

    @After
    public void cleanUp() {
        cleanUpLokalTestDb(namedParameterJdbcTemplate);
    }


    @Test
    public void createFunction_apply__skal_lagre_data_i_lokale_sykefraværstatistikk_tabellen(){
        CreateSykefraværsstatistikkFunction createFunction = utils.getCreateFunction();
        createFunction.apply(
                new SykefraværsstatistikkSektor(
                        2019,
                        1,
                        "3",
                        14,
                        new BigDecimal(55.123),
                        new BigDecimal(856.891)
                )
        );

        List<Sykefraværprosent> list = hentSykefraværprosentSektor(namedParameterJdbcTemplate);
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0)).isEqualTo((new Sykefraværprosent(LABEL, new BigDecimal(55.123), new BigDecimal(856.891))));
    }

    @Test
    public void deleteFunction_apply__skal_slette_data_i_lokale_sykefraværstatistikk_tabellen(){
        lagreSykefraværprosentSektor(namedParameterJdbcTemplate, "1", 2018, 3);
        lagreSykefraværprosentSektor(namedParameterJdbcTemplate, "2", 2018, 3);
        lagreSykefraværprosentSektor(namedParameterJdbcTemplate, "1", 2018, 4);
        lagreSykefraværprosentSektor(namedParameterJdbcTemplate, "2", 2018, 4);
        lagreSykefraværprosentSektor(namedParameterJdbcTemplate, "1", 2019, 1);
        lagreSykefraværprosentSektor(namedParameterJdbcTemplate, "2", 2019, 1);

        DeleteSykefraværsstatistikkFunction deleteFunction = utils.getDeleteFunction();
        deleteFunction.apply(new ÅrstallOgKvartal(2018, 4));

        List<Sykefraværprosent> list = hentSykefraværprosentSektor(namedParameterJdbcTemplate);
        assertThat(list.size()).isEqualTo(4);
    }


    private static List<Sykefraværprosent> hentSykefraværprosentSektor(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {

        return namedParameterJdbcTemplate.query(
                "select * from sykefravar_statistikk_sektor",
                new MapSqlParameterSource(),
                (rs, rowNum) -> new Sykefraværprosent(
                        LABEL,
                        rs.getBigDecimal("tapte_dagsverk"),
                        rs.getBigDecimal("mulige_dagsverk")
                )
        );
    }

    private static void lagreSykefraværprosentSektor(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            String sektor,
            int årstall,
            int kvartal
    ) {
        namedParameterJdbcTemplate.update(
                String.format(
                        "insert into sykefravar_statistikk_sektor " +
                                "(arstall, kvartal, sektor_kode, antall_personer, tapte_dagsverk, mulige_dagsverk) " +
                                "values (%d, %d, %s, 15, 30, 300)",
                        årstall,
                        kvartal,
                        sektor
                ),
                new MapSqlParameterSource()
        );
    }

    private static void cleanUpLokalTestDb(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        namedParameterJdbcTemplate.update(
                "delete from sykefravar_statistikk_sektor",
                new MapSqlParameterSource()
        );
    }

}