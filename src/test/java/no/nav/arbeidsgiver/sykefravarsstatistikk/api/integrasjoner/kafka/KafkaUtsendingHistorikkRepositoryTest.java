package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.KafkaUtsendingHistorikkData;
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

import java.time.LocalDateTime;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.hentAlleKafkaUtsendingHistorikkData;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllEksportDataFraDatabase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfigForJdbcTesterConfig.class})
@DataJdbcTest(excludeAutoConfiguration = {TestDatabaseAutoConfiguration.class})
class KafkaUtsendingHistorikkRepositoryTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private KafkaUtsendingHistorikkRepository kafkaUtsendingHistorikkRepository;

    @BeforeEach
    void setUp() {
        kafkaUtsendingHistorikkRepository = new KafkaUtsendingHistorikkRepository(jdbcTemplate);
        slettAllEksportDataFraDatabase(jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        slettAllEksportDataFraDatabase(jdbcTemplate);
    }


    @Test
    void opprettHistorikk__oppretter_historikk() {
        LocalDateTime startTime = LocalDateTime.now();

        kafkaUtsendingHistorikkRepository.opprettHistorikk(
                "987654321",
                "{\"orgnr\": \"987654321\"}",
                "{\"statistikk\": \"....\"}"
        );

        List<KafkaUtsendingHistorikkData> results = hentAlleKafkaUtsendingHistorikkData(
            jdbcTemplate
        );
        KafkaUtsendingHistorikkData kafkaUtsendingHistorikkData = results.get(0);
        assertEquals("987654321", kafkaUtsendingHistorikkData.orgnr);
        assertEquals("{\"orgnr\": \"987654321\"}", kafkaUtsendingHistorikkData.key);
        assertEquals("{\"statistikk\": \"....\"}", kafkaUtsendingHistorikkData.value);
        assertTrue(kafkaUtsendingHistorikkData.opprettet.isAfter(startTime));
    }
}
