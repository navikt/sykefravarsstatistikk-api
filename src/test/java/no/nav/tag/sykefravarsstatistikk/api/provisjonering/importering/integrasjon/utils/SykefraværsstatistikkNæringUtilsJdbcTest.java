package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils;

import no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning.Sykefraværprosent;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkNæring;
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
    public void createFunction_apply__skal_lagre_data_i_lokale_sykefraværstatistikk_tabellen(){
        CreateSykefraværsstatistikkFunction createFunction = utils.getCreateFunction();
        createFunction.apply(
                new SykefraværsstatistikkNæring(
                        2019,
                        1,
                        "03",
                        14,
                        new BigDecimal(55.123),
                        new BigDecimal(856.891)
                )
        );

        List<Sykefraværprosent> list = hentSykefraværprosentNæring(namedParameterJdbcTemplate);
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0)).isEqualTo((new Sykefraværprosent(LABEL, new BigDecimal(55.123), new BigDecimal(856.891))));
    }

    @Test
    public void createFunction_delete__skal_slette_data_i_lokale_sykefraværstatistikk_tabellen(){
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
                        rs.getBigDecimal("mulige_dagsverk")
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
        delete(namedParameterJdbcTemplate, "naringsgruppe");
    }

    private static void delete(NamedParameterJdbcTemplate namedParameterJdbcTemplate, String tabell) {
        namedParameterJdbcTemplate.update(
                String.format("delete from %s", tabell),
                new MapSqlParameterSource()
        );
    }

    private static void opprettDimensjoner(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        opprettNæringsgruppe(namedParameterJdbcTemplate, "1", "Jordbruk, skogbruk og fiske");
        opprettNæring(namedParameterJdbcTemplate, "01", "1", "Jordbruk");
        opprettNæring(namedParameterJdbcTemplate, "02", "1", "Skogbruk");
        opprettNæring(namedParameterJdbcTemplate, "03", "1", "Akvalultur");
    }

    private static void opprettNæringsgruppe(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            String næringsgruppekode,
            String næringsgruppenavn) {
        namedParameterJdbcTemplate.update(
                String.format(
                        "insert into naringsgruppe (kode, navn) " +
                                "values('%s', '%s')",
                        næringsgruppekode,
                        næringsgruppenavn
                ),
                new MapSqlParameterSource()
        );
    }

    private static void opprettNæring(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            String næringkode,
            String næringsgruppekode,
            String næringnavn) {
        namedParameterJdbcTemplate.update(
                String.format(
                        "insert into naring (kode, naringsgruppe_kode, navn) " +
                                "values('%s', '%s', '%s')",
                        næringkode,
                        næringsgruppekode,
                        næringnavn
                ),
                new MapSqlParameterSource()
        );
    }

}