package no.nav.tag.sykefravarsstatistikk.api.besøksstatistikk;

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

import static no.nav.tag.sykefravarsstatistikk.api.TestUtils.*;
import static org.assertj.core.api.Java6Assertions.assertThat;

@ActiveProfiles("db-test")
@RunWith(SpringRunner.class)
@DataJdbcTest
public class BesøksstatistikkRepositoryJdbcTest {
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private BesøksstatistikkRepository repository;


    @Before
    public void setUp() {
        repository = new BesøksstatistikkRepository(namedParameterJdbcTemplate);
        cleanUpTestDb(namedParameterJdbcTemplate);
    }

    @After
    public void tearDown() {
        cleanUpTestDb(namedParameterJdbcTemplate);
    }

    @Test
    public void sessionIdEksisterer__skal_gi_false_hvis_sessionId_ikke_eksisterer() {
        assertThat(repository.sessionIdEksisterer("sessionId som ikke finnes i db")).isFalse();
    }

    @Test
    public void sessionIdEksisterer__skal_gi_true_hvis_en_stor_virksomhet_har_lik_sessionId() {
        String sessionId = "sessionId til stor virksomhet";
        repository.lagreBesøkFraStorVirksomhet(
                enEnhet(),
                enSektor(),
                enNæringskode5Siffer(),
                enNæring(),
                enSammenligning(),
                sessionId
        );
        assertThat(repository.sessionIdEksisterer(sessionId)).isTrue();
    }

    @Test
    public void sessionIdEksisterer__skal_gi_true_hvis_en_liten_virksomhet_har_lik_sessionId() {
        String sessionId = "sessionId til liten virksomhet";
        repository.lagreBesøkFraLitenVirksomhet(
                sessionId
        );
        assertThat(repository.sessionIdEksisterer(sessionId)).isTrue();
    }


    private static void cleanUpTestDb(NamedParameterJdbcTemplate jdbcTemplate) {
        delete(jdbcTemplate, "besoksstatistikk_virksomhet");
        delete(jdbcTemplate, "besoksstatistikk_smaa_virksomheter");
    }

    private static int delete(NamedParameterJdbcTemplate jdbcTemplate, String tabell) {
        return jdbcTemplate.update(String.format("delete from %s", tabell), new MapSqlParameterSource());
    }

}