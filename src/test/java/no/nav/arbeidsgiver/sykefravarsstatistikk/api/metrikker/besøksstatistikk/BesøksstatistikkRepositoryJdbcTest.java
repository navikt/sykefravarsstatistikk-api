package no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker.besøksstatistikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.LocalOgUnitTestOidcConfiguration;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.altinn.AltinnRolle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;

import static java.lang.String.format;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.enSammenligningBuilder;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.enSammenligningEventBuilder;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.enUnderenhet;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.etOrgnr;
import static org.assertj.core.api.Java6Assertions.assertThat;

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfigForJdbcTesterConfig.class})
@DataJdbcTest(excludeAutoConfiguration = {TestDatabaseAutoConfiguration.class, LocalOgUnitTestOidcConfiguration.class})
public class BesøksstatistikkRepositoryJdbcTest {
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private BesøksstatistikkRepository repository;


    @BeforeEach
    public void setUp() {
        repository = new BesøksstatistikkRepository(namedParameterJdbcTemplate);
        cleanUpTestDb(namedParameterJdbcTemplate);
    }

    @AfterEach
    public void tearDown() {
        cleanUpTestDb(namedParameterJdbcTemplate);
    }

    @Test
    public void sessionIdEksisterer__skal_gi_false_hvis_sessionId_ikke_eksisterer() {
        assertThat(repository.sessionHarBlittRegistrert("sessionId som ikke finnes i db", etOrgnr())).isFalse();
    }

    @Test
    public void sessionIdEksisterer__skal_gi_true_hvis_en_stor_virksomhet_har_lik_sessionId_og_orgnr() {
        String sessionId = "sessionId til stor virksomhet";
        repository.lagreBesøkFraStorVirksomhet(
                enSammenligningEventBuilder()
                        .sessionId(sessionId)
                        .underenhet(enUnderenhet("98765432"))
                        .build()
        );
        assertThat(repository.sessionHarBlittRegistrert(sessionId, new Orgnr("98765432"))).isTrue();
    }

    @Test
    public void sessionIdEksisterer__skal_gi_false_hvis_en_stor_virksomhet_har_lik_sessionId_men_forskjellig_orgnr() {
        String sessionId = "sessionId til stor virksomhet";
        repository.lagreBesøkFraStorVirksomhet(
                enSammenligningEventBuilder()
                        .sessionId(sessionId)
                        .underenhet(enUnderenhet("1111"))
                        .build()
        );
        assertThat(repository.sessionHarBlittRegistrert(sessionId, new Orgnr("9999"))).isFalse();
    }

    @Test
    public void sessionIdEksisterer__skal_gi_true_hvis_en_liten_virksomhet_har_lik_sessionId() {
        String sessionId = "sessionId til liten virksomhet";
        repository.lagreBesøkFraLitenVirksomhet(
                sessionId
        );
        assertThat(repository.sessionHarBlittRegistrert(sessionId, etOrgnr())).isTrue();
    }

    @Test
    public void lagreBesøkFraStorVirksomhet__skal_ikke_feile_hvis_næring_og_bransje_er_null() {
        repository.lagreBesøkFraStorVirksomhet(
                enSammenligningEventBuilder()
                        .bransje(null)
                        .sammenligning(enSammenligningBuilder()
                                .næring(null)
                                .bransje(null)
                                .build())
                        .build()
        );
    }

    @Test
    public void lagreRollerKnyttetTilBesøket__skal_alle_roller_knyttet_et_unikt_besøk() {
        repository.lagreRollerKnyttetTilBesøket(
                2019,
                52, Arrays.asList(
                        altinnRolle("12345", "Regnskap"),
                        altinnRolle("12", "Personalsjef"),
                        altinnRolle("980", "Daglig leder")
                )
        );

        assertThat(antallRaderITabellen("besoksstatistikk_unikt_besok")).isEqualTo(1);
        assertThat(antallRaderITabellen("besoksstatistikk_altinn_roller")).isEqualTo(3);
    }

    @Test
    public void lagreRollerKnyttetTilBesøket__skal_ikke_feile_dersom_besøkende_har_ingen_rolle() {
        repository.lagreRollerKnyttetTilBesøket(2019, 52, Collections.emptyList());

        assertThat(antallRaderITabellen("besoksstatistikk_unikt_besok")).isEqualTo(1);
        assertThat(antallRaderITabellen("besoksstatistikk_altinn_roller")).isEqualTo(0);
    }


    private static void cleanUpTestDb(NamedParameterJdbcTemplate jdbcTemplate) {
        delete(jdbcTemplate, "besoksstatistikk_virksomhet");
        delete(jdbcTemplate, "besoksstatistikk_smaa_virksomheter");
        delete(jdbcTemplate, "besoksstatistikk_altinn_roller");
        delete(jdbcTemplate, "besoksstatistikk_unikt_besok");
    }

    private static int delete(NamedParameterJdbcTemplate jdbcTemplate, String tabell) {
        return jdbcTemplate.update(format("delete from %s", tabell), new MapSqlParameterSource());
    }

    private int antallRaderITabellen(String tabbelnavn) {
        Integer antallRader = namedParameterJdbcTemplate.queryForObject(
                format("select count(*) from %s", tabbelnavn),
                new MapSqlParameterSource(),
                Integer.class
        );
        return antallRader == null ? 0 : antallRader;
    }

    private static AltinnRolle altinnRolle(String definitionId, String name) {
        AltinnRolle altinnRolle = new AltinnRolle();
        altinnRolle.setDefinitionId(definitionId);
        altinnRolle.setName(name);
        return altinnRolle;
    }
}
