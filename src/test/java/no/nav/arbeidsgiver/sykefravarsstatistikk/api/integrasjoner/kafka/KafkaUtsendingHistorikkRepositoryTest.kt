package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.KafkaUtsendingHistorikkData
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.hentAlleKafkaUtsendingHistorikkData
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.opprettUtsendingHistorikk
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllEksportDataFraDatabase
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.KafkaUtsendingHistorikkRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class KafkaUtsendingHistorikkRepositoryTest {
    @Autowired
    private val jdbcTemplate: NamedParameterJdbcTemplate? = null
    private var kafkaUtsendingHistorikkRepository: KafkaUtsendingHistorikkRepository? = null
    @BeforeEach
    fun setUp() {
        kafkaUtsendingHistorikkRepository = KafkaUtsendingHistorikkRepository(jdbcTemplate)
        slettAllEksportDataFraDatabase(jdbcTemplate!!)
    }

    @AfterEach
    fun tearDown() {
        slettAllEksportDataFraDatabase(jdbcTemplate!!)
    }

    @Test
    fun opprettHistorikk__oppretter_historikk() {
        val startTime = LocalDateTime.now()
        kafkaUtsendingHistorikkRepository!!.opprettHistorikk(
            "987654321", "{\"orgnr\": \"987654321\"}", "{\"statistikk\": \"....\"}"
        )
        val results = hentAlleKafkaUtsendingHistorikkData(jdbcTemplate!!)
        val kafkaUtsendingHistorikkData = results[0]
        Assertions.assertEquals("987654321", kafkaUtsendingHistorikkData.orgnr)
        Assertions.assertEquals("{\"orgnr\": \"987654321\"}", kafkaUtsendingHistorikkData.key)
        Assertions.assertEquals("{\"statistikk\": \"....\"}", kafkaUtsendingHistorikkData.value)
        Assertions.assertTrue(kafkaUtsendingHistorikkData.opprettet.isAfter(startTime))
    }

    @Test
    fun slettHistorikk__sletter_historikk() {
        opprettUtsendingHistorikk(
            jdbcTemplate!!, KafkaUtsendingHistorikkData("988777999", "key", "value", LocalDateTime.now())
        )
        opprettUtsendingHistorikk(
            jdbcTemplate, KafkaUtsendingHistorikkData("988999777", "key", "value", LocalDateTime.now())
        )
        Assertions.assertEquals(2, hentAlleKafkaUtsendingHistorikkData(jdbcTemplate).size)
        val antallSlettet = kafkaUtsendingHistorikkRepository!!.slettHistorikk()
        val results = hentAlleKafkaUtsendingHistorikkData(jdbcTemplate)
        Assertions.assertEquals(0, results.size)
        Assertions.assertEquals(2, antallSlettet)
    }
}
