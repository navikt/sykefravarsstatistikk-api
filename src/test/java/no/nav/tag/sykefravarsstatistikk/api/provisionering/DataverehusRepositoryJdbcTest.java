package no.nav.tag.sykefravarsstatistikk.api.provisionering;

import no.nav.tag.sykefravarsstatistikk.api.domene.klassifikasjoner.Sektor;
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

import static org.junit.Assert.assertTrue;

@ActiveProfiles("db-test")
@RunWith(SpringRunner.class)
@DataJdbcTest
public class DataverehusRepositoryJdbcTest {


    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private DataverehusRepository repository;

    @Before
    public void setUp() {
        repository = new DataverehusRepository(namedParameterJdbcTemplate);
        cleanUpTestDb(namedParameterJdbcTemplate);
    }

    @After
    public void tearDown() {
        cleanUpTestDb(namedParameterJdbcTemplate);
    }


    @Test
    public void hentSektor_returnerer_eksisterende_Sektor() {
        insertSektorInDvhTabell(namedParameterJdbcTemplate, "9", "Fylkeskommunal forvaltning");
        insertSektorInDvhTabell(namedParameterJdbcTemplate, "0", "Ukjent");

        List<Sektor> sektorer = repository.hentAlleSektorer();

        assertTrue(sektorer.size() == 2);
        assertTrue(sektorer.contains(new Sektor("0", "Ukjent")));
    }


    private static void cleanUpTestDb(NamedParameterJdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("delete from SEKTOR__TEST", new MapSqlParameterSource());
        jdbcTemplate.update("delete from dt_p.V_DIM_IA_SEKTOR", new MapSqlParameterSource());
    }

    private static void insertSektorInDvhTabell(NamedParameterJdbcTemplate jdbcTemplate, String kode, String navn) {
        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("sektorkode", kode)
                        .addValue("sektornavn", navn);

        jdbcTemplate.update("insert into dt_p.V_DIM_IA_SEKTOR (SEKTORKODE, SEKTORNAVN) values (:sektorkode, :sektornavn)", params);
    }
}