package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils;

import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkVirksomhet;
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
public class SykefraværsstatistikkVirksomhetUtilsJdbcTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private SykefraværsstatistikkVirksomhetUtils utils;

    private static final String LABEL = "TEST";


    @Before
    public void setUp() {
        utils = new SykefraværsstatistikkVirksomhetUtils(namedParameterJdbcTemplate);
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
                new SykefraværsstatistikkVirksomhet(
                        2019,
                        1,
                        "987654321",
                        14,
                        new BigDecimal(55.123),
                        new BigDecimal(856.891)
                )
        );

        List<Sykefraværprosent> list = hentSykefraværprosentVirksomhet(namedParameterJdbcTemplate);
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0)).isEqualTo((new Sykefraværprosent(LABEL, new BigDecimal(55.123), new BigDecimal(856.891), 14)));
    }

    @Test
    public void createFunction_delete__skal_slette_data_i_lokale_sykefraværstatistikk_tabellen(){
        lagreSykefraværprosentVirksomhet(namedParameterJdbcTemplate, "987654321", 2018, 3);
        lagreSykefraværprosentVirksomhet(namedParameterJdbcTemplate, "987654321", 2018, 4);
        lagreSykefraværprosentVirksomhet(namedParameterJdbcTemplate, "987654321", 2019, 1);

        DeleteSykefraværsstatistikkFunction deleteFunction = utils.getDeleteFunction();
        deleteFunction.apply(new ÅrstallOgKvartal(2018, 4));

        List<Sykefraværprosent> list = hentSykefraværprosentVirksomhet(namedParameterJdbcTemplate);
        assertThat(list.size()).isEqualTo(2);
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