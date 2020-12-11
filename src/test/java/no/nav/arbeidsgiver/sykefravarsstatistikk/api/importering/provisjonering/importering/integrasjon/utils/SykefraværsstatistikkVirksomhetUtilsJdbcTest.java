package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.importering.integrasjon.utils;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.SykefraværsstatistikkVirksomhetUtils;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.besøksstatistikk.sammenligning.Sykefraværprosent;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkVirksomhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.statistikk.BatchCreateSykefraværsstatistikkFunction;
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
import java.util.stream.IntStream;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori._1_DAG_TIL_7_DAGER;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("db-test")
@DataJdbcTest
public class SykefraværsstatistikkVirksomhetUtilsJdbcTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private SykefraværsstatistikkVirksomhetUtils utils;

    private static final String LABEL = "TEST";


    @BeforeEach
    public void setUp() {
        utils = new SykefraværsstatistikkVirksomhetUtils(namedParameterJdbcTemplate);
        cleanUpLokalTestDb(namedParameterJdbcTemplate);
    }

    @AfterEach
    public void cleanUp() {
        cleanUpLokalTestDb(namedParameterJdbcTemplate);
    }


    @Test
    public void createFunction_apply__skal_lagre_data_i_lokale_sykefraværstatistikk_tabellen() {

        List<SykefraværsstatistikkVirksomhet> list = new ArrayList<>();
        list.add(new SykefraværsstatistikkVirksomhet(
                2019,
                1,
                "987654321",
                _1_DAG_TIL_7_DAGER.kode,
                14,
                new BigDecimal(55.123),
                new BigDecimal(856.891)
        ));

        BatchCreateSykefraværsstatistikkFunction createFunction = utils.getBatchCreateFunction(list);
        createFunction.apply();

        List<Sykefraværprosent> resultList = hentSykefraværprosentVirksomhet(namedParameterJdbcTemplate);
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
    public void createFunction_apply__ytelsestest() {
        utils.getBatchCreateFunction(createStatistikk(1000)).apply();

        List<Sykefraværprosent> resultList = hentSykefraværprosentVirksomhet(namedParameterJdbcTemplate);
        assertThat(resultList.size()).isEqualTo(1000);
    }


    @Test
    public void createFunction_delete__skal_slette_data_i_lokale_sykefraværstatistikk_tabellen() {
        lagreSykefraværprosentVirksomhet(namedParameterJdbcTemplate, "987654321", 2018, 3);
        lagreSykefraværprosentVirksomhet(namedParameterJdbcTemplate, "987654321", 2018, 4);
        lagreSykefraværprosentVirksomhet(namedParameterJdbcTemplate, "987654321", 2019, 1);

        DeleteSykefraværsstatistikkFunction deleteFunction = utils.getDeleteFunction();
        deleteFunction.apply(new ÅrstallOgKvartal(2018, 4));

        List<Sykefraværprosent> list = hentSykefraværprosentVirksomhet(namedParameterJdbcTemplate);
        assertThat(list.size()).isEqualTo(2);
    }


    private List<SykefraværsstatistikkVirksomhet> createStatistikk(int antall) {
        List<SykefraværsstatistikkVirksomhet> list = new ArrayList<>();

        IntStream.range(0, antall).forEach(
                i -> list.add(new SykefraværsstatistikkVirksomhet(
                                2019,
                                1,
                                Integer.valueOf(987000000 + i).toString(),
                        _1_DAG_TIL_7_DAGER.kode,
                                14,
                                new BigDecimal(55.123),
                                new BigDecimal(856.891)
                        )
                )
        );
        return list;
    }

    private static List<Sykefraværprosent> hentSykefraværprosentVirksomhet(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {

        return namedParameterJdbcTemplate.query(
                "select * from sykefravar_statistikk_virksomhet",
                new MapSqlParameterSource(),
                (rs, rowNum) -> new Sykefraværprosent(
                        LABEL,
                        rs.getBigDecimal("tapte_dagsverk"),
                        rs.getBigDecimal("mulige_dagsverk"),
                        rs.getInt("antall_personer")
                )
        );
    }

    private static void lagreSykefraværprosentVirksomhet(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            String orgnr,
            int årstall,
            int kvartal
    ) {
        namedParameterJdbcTemplate.update(
                String.format(
                        "insert into sykefravar_statistikk_virksomhet " +
                                "(arstall, kvartal, orgnr, antall_personer, tapte_dagsverk, mulige_dagsverk) " +
                                "values (%d, %d, '%s', 15, 30, 300)",
                        årstall,
                        kvartal,
                        orgnr
                ),
                new MapSqlParameterSource()
        );
    }

    private static void cleanUpLokalTestDb(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        delete(namedParameterJdbcTemplate, "sykefravar_statistikk_virksomhet");
    }

    private static void delete(NamedParameterJdbcTemplate namedParameterJdbcTemplate, String tabell) {
        namedParameterJdbcTemplate.update(
                String.format("delete from %s", tabell),
                new MapSqlParameterSource()
        );
    }

}
