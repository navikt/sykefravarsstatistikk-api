package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.virksomhetsklassifikasjoner;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("db-test")
@RunWith(SpringRunner.class)
@DataJdbcTest
public class KlassifikasjonsimporteringRepositoryJdbcTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private KlassifikasjonerRepository repository;

    @Before
    public void setUp() {
        repository = new KlassifikasjonerRepository(namedParameterJdbcTemplate);
        cleanUpTestDb(namedParameterJdbcTemplate);
    }

    @After
    public void tearDown() {
        cleanUpTestDb(namedParameterJdbcTemplate);
    }


    @Test
    public void hentSektor_returnerer_eksisterende_Sektor() {
        insertSektor(namedParameterJdbcTemplate, "9", "Fylkeskommunal forvaltning");

        Sektor sektor = repository.hentSektor("9");

        assertThat(sektor.getKode()).isEqualTo("9");
        assertThat(sektor.getNavn()).isEqualTo("Fylkeskommunal forvaltning");
    }

    private static void cleanUpTestDb(NamedParameterJdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("delete from sektor", new MapSqlParameterSource());
    }

    private static void insertSektor(NamedParameterJdbcTemplate jdbcTemplate, String kode, String navn) {
        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("kode", kode)
                        .addValue("navn", navn);

        jdbcTemplate.update("insert into sektor (kode, navn) values (:kode, :navn)", params);
    }
}
