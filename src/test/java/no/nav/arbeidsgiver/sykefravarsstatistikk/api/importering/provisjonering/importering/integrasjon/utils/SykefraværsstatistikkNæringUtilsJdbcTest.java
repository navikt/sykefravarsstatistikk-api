package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.importering.integrasjon.utils;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.utils.SykefraværsstatistikkNæringUtils;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.besøksstatistikk.sammenligning.Sykefraværprosent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.utils.DeleteSykefraværsstatistikkFunction;
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
public class SykefraværsstatistikkNæringUtilsJdbcTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private SykefraværsstatistikkNæringUtils utils;

    private static final String LABEL = "TEST";


    @Before
    public void setUp() {
        utils = new SykefraværsstatistikkNæringUtils(namedParameterJdbcTemplate);
        cleanUpLokalTestDb(namedParameterJdbcTemplate);
        opprettDimensjoner(namedParameterJdbcTemplate);
    }

    @After
    public void cleanUp() {
        cleanUpLokalTestDb(namedParameterJdbcTemplate);
    }


    @Test
    public void createFunction_apply__skal_lagre_data_i_lokale_sykefraværstatistikk_tabellen() {
        List<SykefraværsstatistikkNæring> list = new ArrayList<>();
        list.add(
                new SykefraværsstatistikkNæring(
                        2019,
                        1,
                        "03",
                        14,
                        new BigDecimal(55.123),
                        new BigDecimal(856.891)
                )
        );

        utils.getBatchCreateFunction(list).apply();

        List<Sykefraværprosent> resultList = hentSykefraværprosentNæring(namedParameterJdbcTemplate);
        assertThat(resultList.size()).isEqualTo(1);
        assertThat(resultList.get(0)).isEqualTo(
                new Sykefraværprosent(
                        LABEL,
                        new BigDecimal(55.123),
                        new BigDecimal(856.891),
                        14
                )
        );
    }

    @Test
    public void createFunction_delete__skal_slette_data_i_lokale_sykefraværstatistikk_tabellen() {
        lagreSykefraværprosentNæring(namedParameterJdbcTemplate, "01", 2018, 3);
        lagreSykefraværprosentNæring(namedParameterJdbcTemplate, "02", 2018, 3);
        lagreSykefraværprosentNæring(namedParameterJdbcTemplate, "01", 2018, 4);
        lagreSykefraværprosentNæring(namedParameterJdbcTemplate, "02", 2018, 4);
        lagreSykefraværprosentNæring(namedParameterJdbcTemplate, "01", 2019, 1);
        lagreSykefraværprosentNæring(namedParameterJdbcTemplate, "02", 2019, 1);

        DeleteSykefraværsstatistikkFunction deleteFunction = utils.getDeleteFunction();
        deleteFunction.apply(new ÅrstallOgKvartal(2018, 4));

        List<Sykefraværprosent> list = hentSykefraværprosentNæring(namedParameterJdbcTemplate);
        assertThat(list.size()).isEqualTo(4);
    }


    private static List<Sykefraværprosent> hentSykefraværprosentNæring(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {

        return namedParameterJdbcTemplate.query(
                "select * from sykefravar_statistikk_naring",
                new MapSqlParameterSource(),
                (rs, rowNum) -> new Sykefraværprosent(
                        LABEL,
                        rs.getBigDecimal("tapte_dagsverk"),
                        rs.getBigDecimal("mulige_dagsverk"),
                        rs.getInt("antall_personer")
                )
        );
    }

    private static void lagreSykefraværprosentNæring(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            String næringkode,
            int årstall,
            int kvartal
    ) {
        namedParameterJdbcTemplate.update(
                String.format(
                        "insert into sykefravar_statistikk_naring " +
                                "(arstall, kvartal, naring_kode, antall_personer, tapte_dagsverk, mulige_dagsverk) " +
                                "values (%d, %d, '%s', 15, 30, 300)",
                        årstall,
                        kvartal,
                        næringkode
                ),
                new MapSqlParameterSource()
        );
    }

    private static void cleanUpLokalTestDb(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        delete(namedParameterJdbcTemplate, "sykefravar_statistikk_naring");
        delete(namedParameterJdbcTemplate, "naring");
    }

    private static void delete(NamedParameterJdbcTemplate namedParameterJdbcTemplate, String tabell) {
        namedParameterJdbcTemplate.update(
                String.format("delete from %s", tabell),
                new MapSqlParameterSource()
        );
    }

    private static void opprettDimensjoner(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        opprettNæring(namedParameterJdbcTemplate, "01", "Jordbruk");
        opprettNæring(namedParameterJdbcTemplate, "02", "Skogbruk");
        opprettNæring(namedParameterJdbcTemplate, "03", "Akvalultur");
    }

    private static void opprettNæring(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            String næringkode,
            String næringnavn) {
        namedParameterJdbcTemplate.update(
                String.format(
                        "insert into naring (kode, navn) " +
                                "values('%s', '%s')",
                        næringkode,
                        næringnavn
                ),
                new MapSqlParameterSource()
        );
    }

}
