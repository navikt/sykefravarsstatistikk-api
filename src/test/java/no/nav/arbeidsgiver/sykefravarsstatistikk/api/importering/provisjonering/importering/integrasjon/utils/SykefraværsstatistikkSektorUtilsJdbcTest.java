package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.importering.integrasjon.utils;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.SykefraværsstatistikkSektorUtils;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.besøksstatistikk.sammenligning.Sykefraværprosent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkSektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.DeleteSykefraværsstatistikkFunction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
@DataJdbcTest
public class SykefraværsstatistikkSektorUtilsJdbcTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private SykefraværsstatistikkSektorUtils utils;

    private static final String LABEL = "TEST";


    @BeforeEach
    public void setUp() {
        utils = new SykefraværsstatistikkSektorUtils(namedParameterJdbcTemplate);
        cleanUpLokalTestDb(namedParameterJdbcTemplate);
    }

    @AfterEach
    public void cleanUp() {
        cleanUpLokalTestDb(namedParameterJdbcTemplate);
    }


    @Test
    public void createFunction_apply__skal_lagre_data_i_lokale_sykefraværstatistikk_tabellen() {
        List<SykefraværsstatistikkSektor> list = new ArrayList<>();
        list.add(
                new SykefraværsstatistikkSektor(
                        2019,
                        1,
                        "3",
                        14,
                        new BigDecimal(55.123),
                        new BigDecimal(856.891)
                )

        );

        utils.getBatchCreateFunction(list).apply();

        List<Sykefraværprosent> resultList = hentSykefraværprosentSektor(namedParameterJdbcTemplate);
        assertThat(resultList.size()).isEqualTo(1);
        assertThat(resultList.get(0)).isEqualTo(
                new Sykefraværprosent(
                        LABEL,
                        new BigDecimal(55.123),
                        new BigDecimal(856.891),
                        14
                )
        );

        assertThat(resultList.size()).isEqualTo(1);
        assertThat(resultList.get(0)).isEqualTo((new Sykefraværprosent(LABEL, new BigDecimal(55.123), new BigDecimal(856.891), 14)));
    }

    @Test
    public void deleteFunction_apply__skal_slette_data_i_lokale_sykefraværstatistikk_tabellen() {
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
                        rs.getBigDecimal("mulige_dagsverk"),
                        rs.getInt("antall_personer")
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
