package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka

import config.AppConfigForJdbcTesterConfig
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.date.shouldBeBefore
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.sql.selectAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import testUtils.TestUtils.slettAllEksportDataFraDatabase
import java.time.LocalDateTime

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class KafkaUtsendingHistorikkRepositoryTest {
    @Autowired
    private lateinit var kafkaUtsendingHistorikkRepository: KafkaUtsendingHistorikkRepository

    @BeforeEach
    fun setUp() {
        slettAllEksportDataFraDatabase(kafkaUtsendingHistorikkRepository = kafkaUtsendingHistorikkRepository)
    }

    @Test
    fun opprettHistorikk__oppretter_historikk() {
        val startTime = LocalDateTime.now()
        kafkaUtsendingHistorikkRepository.opprettHistorikk(
            "987654321", "{\"orgnr\": \"987654321\"}", "{\"statistikk\": \"....\"}"
        )
        val results = kafkaUtsendingHistorikkRepository.hentAlt()
        val kafkaUtsendingHistorikkData = results[0]
        kafkaUtsendingHistorikkData.orgnr shouldBe "987654321"
        kafkaUtsendingHistorikkData.key shouldBe "{\"orgnr\": \"987654321\"}"
        kafkaUtsendingHistorikkData.value shouldBe "{\"statistikk\": \"....\"}"
        kafkaUtsendingHistorikkData.opprettet shouldBeBefore startTime
    }

    @Test
    fun slettHistorikk__sletter_historikk() {
        kafkaUtsendingHistorikkRepository.opprettHistorikk(
            "987654321", "{\"orgnr\": \"987654321\"}", "{\"statistikk\": \"....\"}"
        )
        kafkaUtsendingHistorikkRepository.opprettHistorikk(
            "123456789", "{\"orgnr\": \"123456789\"}", "{\"statistikk\": \"....\"}"
        )

        val førSletting = kafkaUtsendingHistorikkRepository.hentAlt()
        førSletting shouldHaveSize 2

        val antallSlettet = kafkaUtsendingHistorikkRepository.slettHistorikk()
        antallSlettet shouldBe 2

        val etterSletting = kafkaUtsendingHistorikkRepository.hentAlt()
        etterSletting shouldHaveSize 0
    }

    private fun KafkaUtsendingHistorikkRepository.hentAlt(): List<KafkaUtsendingHistorikkData> {
        return transaction {
            selectAll().map {
                KafkaUtsendingHistorikkData(
                    orgnr = it[orgnr], key = it[key], value = it[value], opprettet = it[opprettet]
                )
            }
        }
    }
}
