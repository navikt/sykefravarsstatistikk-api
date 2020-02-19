package no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.utils;

import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.CreateVirksomhetsklassifikasjonFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.FetchVirksomhetsklassifikasjonFunction;
import no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.UpdateVirksomhetsklassifikasjonFunction;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("db-test")
@RunWith(SpringRunner.class)
@DataJdbcTest
public class NæringIntegrasjonUtilsTest {


    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private NæringIntegrasjonUtils utils;

    private static final String LABEL = "TEST";


    @Before
    public void setUp() {
        utils = new NæringIntegrasjonUtils(namedParameterJdbcTemplate);
        cleanUpLokalTestDb(namedParameterJdbcTemplate);
    }

    @After
    public void cleanUp() {
        cleanUpLokalTestDb(namedParameterJdbcTemplate);
    }


    @Test
    public void createFunction_apply__skal_lagre_data_i_lokale_næring_tabellen() {
        CreateVirksomhetsklassifikasjonFunction createFunction = utils.getCreateFunction();

        createFunction.apply(new Næring("01", "Jordbruk"));

        List<Næring> næringList = hentNæring(namedParameterJdbcTemplate);
        assertThat(næringList.size()).isEqualTo(1);
        assertThat(næringList.get(0)).isEqualTo((new Næring("01", "Jordbruk")));
    }

    @Test
    public void fetchFunction_apply__skal_hente_data_i_lokale_næring_tabellen() {
        opprettNæring(namedParameterJdbcTemplate, "01", "Jordbruk");
        FetchVirksomhetsklassifikasjonFunction fetchFunction = utils.getFetchFunction();

        Optional<Næring> næring = fetchFunction.apply(new Næring("01", "Jordbruk"));

        assertThat(næring.isPresent()).isTrue();
        assertThat(næring.get()).isEqualTo((new Næring("01", "Jordbruk")));
    }

    @Test
    public void updateFunction_apply__skal_oppdatere_data_i_lokale_næring_tabellen() {
        opprettNæring(namedParameterJdbcTemplate, "01", "Jordbruk");
        UpdateVirksomhetsklassifikasjonFunction updateFunction = utils.getUpdateFunction();

        int antallOppdatert = updateFunction.apply(
                new Næring("01", "Jordbruk"),
                new Næring("01", "Jordbruk og andre ting")
        );

        Næring oppdatertNæring = hentNæring(namedParameterJdbcTemplate).get(0);
        assertThat(antallOppdatert).isEqualTo(1);
        assertThat(oppdatertNæring).isEqualTo((new Næring("01", "Jordbruk og andre ting")));
    }


    private static void cleanUpLokalTestDb(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        delete(namedParameterJdbcTemplate, "naring");
    }

    private static void delete(NamedParameterJdbcTemplate namedParameterJdbcTemplate, String tabell) {
        namedParameterJdbcTemplate.update(
                String.format("delete from %s", tabell),
                new MapSqlParameterSource()
        );
    }

    private static List<Næring> hentNæring(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {

        return namedParameterJdbcTemplate.query(
                "select * from naring",
                new MapSqlParameterSource(),
                (rs, rowNum) -> new Næring(
                        rs.getString("kode"),
                        rs.getString("navn")
                )
        );
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
