package infrastruktur.database

import config.AppConfigForJdbcTesterConfig
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportAvSykefraværsstatistikk.domene.VirksomhetEksportPerKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.Orgnr
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.fellesdomene.ÅrstallOgKvartal
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.LegacyEksporteringRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka.LegacyKafkaUtsendingHistorikkRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import testUtils.TestData.ORGNR_VIRKSOMHET_1
import testUtils.TestData.ORGNR_VIRKSOMHET_2
import testUtils.TestData.ORGNR_VIRKSOMHET_3
import testUtils.TestUtils.slettAllEksportDataFraDatabase
import java.sql.ResultSet
import java.time.LocalDateTime

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class LegacyEksporteringRepositoryTest {
    @Autowired
    private lateinit var jdbcTemplate: NamedParameterJdbcTemplate

    @Autowired
    private lateinit var legacyEksporteringRepository: LegacyEksporteringRepository

    @Autowired
    private lateinit var legacyKafkaUtsendingHistorikkRepository: LegacyKafkaUtsendingHistorikkRepository

    @BeforeEach
    fun setUp() {
        slettAllEksportDataFraDatabase(jdbcTemplate, legacyKafkaUtsendingHistorikkRepository)
    }

    @Test
    fun hentVirksomhetEksportPerKvartal__returnerer_antall_VirksomhetEksportPerKvartal_funnet() {
        createVirksomhetEksportPerKvartal(
            VirksomhetEksportPerKvartalMedDatoer(
                Orgnr("999999999"),
                ÅrstallOgKvartal(2019, 2),
                true,
                LocalDateTime.now()
            )
        )
        createVirksomhetEksportPerKvartal(
            VirksomhetEksportPerKvartalMedDatoer(
                Orgnr("999999998"),
                ÅrstallOgKvartal(2019, 2),
                false,
                null
            )
        )
        createVirksomhetEksportPerKvartal(
            VirksomhetEksportPerKvartalMedDatoer(
                Orgnr("999999998"),
                ÅrstallOgKvartal(2019, 3),
                true,
                LocalDateTime.now()
            )
        )
        val resultat = legacyEksporteringRepository.hentVirksomhetEksportPerKvartal(ÅrstallOgKvartal(2019, 2))
        Assertions.assertEquals(2, resultat.size)
        Assertions.assertTrue(
            resultat.stream()
                .anyMatch { virksomhetEksportPerKvartal: VirksomhetEksportPerKvartal -> virksomhetEksportPerKvartal.getOrgnr() == "999999998" && !virksomhetEksportPerKvartal.eksportert() && virksomhetEksportPerKvartal.ÅrstallOgKvartal.årstall == 2019 && virksomhetEksportPerKvartal.ÅrstallOgKvartal.kvartal == 2 })
        Assertions.assertTrue(
            resultat.stream()
                .anyMatch { virksomhetEksportPerKvartal: VirksomhetEksportPerKvartal -> virksomhetEksportPerKvartal.getOrgnr() == "999999999" && virksomhetEksportPerKvartal.eksportert() && virksomhetEksportPerKvartal.ÅrstallOgKvartal.årstall == 2019 && virksomhetEksportPerKvartal.ÅrstallOgKvartal.kvartal == 2 })
    }

    @Test
    fun oppdaterOgSetErEksportertTilTrue__skal_returnere_0_hvis_data_ikke_finnes() {
        var oppdaterteRader = legacyEksporteringRepository.opprettEksport(null)
        Assertions.assertEquals(0, oppdaterteRader)
        oppdaterteRader = legacyEksporteringRepository.opprettEksport(emptyList<VirksomhetEksportPerKvartal>())
        Assertions.assertEquals(0, oppdaterteRader)
    }

    @Test
    fun batchOpprettVirksomheterBekreftetEksportert__oppretter_ingenting_hvis_lista_er_tom() {
        val virksomheterBekreftetEksportert: List<String?> = ArrayList()
        legacyEksporteringRepository.batchOpprettVirksomheterBekreftetEksportert(
            virksomheterBekreftetEksportert, ÅrstallOgKvartal(2020, 2)
        )
        val results = hentAlleVirksomhetBekreftetEksportert()
        Assertions.assertEquals(0, results.size)
    }

    @Test
    fun batchOpprettVirksomheterBekreftetEksportert__opprett_i_batch() {
        val virksomheterBekreftetEksportert: MutableList<String?> = ArrayList()
        virksomheterBekreftetEksportert.add(ORGNR_VIRKSOMHET_1)
        virksomheterBekreftetEksportert.add(ORGNR_VIRKSOMHET_2)
        virksomheterBekreftetEksportert.add(ORGNR_VIRKSOMHET_3)
        legacyEksporteringRepository.batchOpprettVirksomheterBekreftetEksportert(
            virksomheterBekreftetEksportert, ÅrstallOgKvartal(2020, 2)
        )
        val results = hentAlleVirksomhetBekreftetEksportert()
        Assertions.assertEquals(3, results.size)
        assertVirksomhetBekreftetEksportert(results, ORGNR_VIRKSOMHET_1)
        assertVirksomhetBekreftetEksportert(results, ORGNR_VIRKSOMHET_2)
        assertVirksomhetBekreftetEksportert(results, ORGNR_VIRKSOMHET_3)
    }

    @Test
    fun oppdaterVirksomheterIEksportTabell__oppdater_virksomheter_som_er_bekreftet_eksportert_og_returnerer_antall_oppdatert() {
        val testStartDato = LocalDateTime.now()
        createVirksomhetBekreftetEksportert(
            VirksomhetBekreftetEksportert(ORGNR_1, _2021_1, testStartDato)
        )
        createVirksomhetBekreftetEksportert(
            VirksomhetBekreftetEksportert(ORGNR_2, _2021_1, testStartDato)
        )
        createVirksomhetEksportPerKvartal(
            VirksomhetEksportPerKvartalMedDatoer(
                ORGNR_1, _2021_1, true, testStartDato
            )
        )
        createVirksomhetEksportPerKvartal(
            VirksomhetEksportPerKvartalMedDatoer(ORGNR_2, _2021_1, false, null)
        )
        createVirksomhetEksportPerKvartal(
            VirksomhetEksportPerKvartalMedDatoer(ORGNR_3, _2021_1, false, null)
        )
        val antallOppdatert =
            legacyEksporteringRepository.oppdaterAlleVirksomheterIEksportTabellSomErBekrreftetEksportert()
        Assertions.assertEquals(1, antallOppdatert)
        val results = hentAlleVirksomhetEksportPerKvartal()
        assertVirksomhetEksportPerKvartal(results, ORGNR_1.verdi, true, testStartDato)
        assertVirksomhetEksportPerKvartal(results, ORGNR_2.verdi, true, testStartDato, true)
        assertVirksomhetEksportPerKvartal(results, ORGNR_3.verdi, false, testStartDato)
    }

    @Test
    fun slettVirksomheterBekreftetEksportert__sletter_alle_rader_i_tabellen_og_returnerer_antall_slettet() {
        createVirksomhetBekreftetEksportert(
            VirksomhetBekreftetEksportert(
                Orgnr(ORGNR_VIRKSOMHET_1), ÅrstallOgKvartal(2020, 1), LocalDateTime.now()
            )
        )
        createVirksomhetBekreftetEksportert(
            VirksomhetBekreftetEksportert(
                Orgnr(ORGNR_VIRKSOMHET_2), ÅrstallOgKvartal(2020, 1), LocalDateTime.now()
            )
        )
        val antallSlettet = legacyEksporteringRepository.slettVirksomheterBekreftetEksportert()
        Assertions.assertEquals(2, antallSlettet)
        Assertions.assertEquals(0, hentAlleVirksomhetBekreftetEksportert().size)
    }

    @Test
    fun hentAntallIkkeEksportertRader__skal_retunere_riktig_tall() {
        opprettTestVirksomhetMetaData(jdbcTemplate, 2020, 2, ORGNR_VIRKSOMHET_1)
        opprettTestVirksomhetMetaData(jdbcTemplate, 2020, 2, ORGNR_VIRKSOMHET_2)
        opprettTestVirksomhetMetaData(jdbcTemplate, 2020, 2, ORGNR_VIRKSOMHET_3, true)
        val antallIkkeFerdigEksportert = legacyEksporteringRepository.hentAntallIkkeFerdigEksportert()
        Assertions.assertEquals(2, antallIkkeFerdigEksportert)
    }

    @Test
    fun slettEksportertPerKvartal__skal_slette_alt() {
        opprettTestVirksomhetMetaData(jdbcTemplate, 2020, 2, ORGNR_VIRKSOMHET_1)
        opprettTestVirksomhetMetaData(jdbcTemplate, 2020, 2, ORGNR_VIRKSOMHET_2)
        opprettTestVirksomhetMetaData(jdbcTemplate, 2020, 2, ORGNR_VIRKSOMHET_3, true)
        val antallSlettet = legacyEksporteringRepository.slettEksportertPerKvartal()
        Assertions.assertEquals(3, antallSlettet)
        val results = hentAlleVirksomhetEksportPerKvartal()
        Assertions.assertEquals(0, results.size)
    }

    private fun assertVirksomhetEksportPerKvartal(
        results: List<VirksomhetEksportPerKvartalMedDatoer>,
        orgnr: String,
        expectedEksportert: Boolean,
        oppdatertEtterDato: LocalDateTime,
        sjekkOppdatertDatoErEndret: Boolean = false
    ) {
        val actual =
            results.stream().filter { v: VirksomhetEksportPerKvartalMedDatoer -> v.orgnr.verdi == orgnr }.findFirst()
                .get()
        Assertions.assertEquals(expectedEksportert, actual.eksportert)
        if (!expectedEksportert) {
            Assertions.assertNull(actual.oppdatert)
        }
        if (sjekkOppdatertDatoErEndret) {
            Assertions.assertEquals(true, actual.oppdatert!!.isAfter(oppdatertEtterDato))
        }
    }

    private fun assertVirksomhetBekreftetEksportert(
        results: List<VirksomhetBekreftetEksportert>, orgnr: String
    ) {
        val actual =
            results.stream().filter { v: VirksomhetBekreftetEksportert -> v.orgnr.verdi == orgnr }.findFirst().get()
        Assertions.assertEquals(orgnr, actual.orgnr.verdi)
    }

    private fun createVirksomhetEksportPerKvartal(virksomhet: VirksomhetEksportPerKvartalMedDatoer): Int {
        val parametre = MapSqlParameterSource()
            .addValue("orgnr", virksomhet.orgnr.verdi)
            .addValue("årstall", virksomhet.årstallOgKvartal.årstall)
            .addValue("kvartal", virksomhet.årstallOgKvartal.kvartal)
            .addValue("eksportert", virksomhet.eksportert)
            .addValue("oppdatert", virksomhet.oppdatert)
        return jdbcTemplate.update(
            "insert into eksport_per_kvartal (orgnr, arstall, kvartal, eksportert, oppdatert) "
                    + "values (:orgnr, :årstall, :kvartal, :eksportert, :oppdatert)",
            parametre
        )
    }

    private fun createVirksomhetBekreftetEksportert(virksomhet: VirksomhetBekreftetEksportert): Int {
        val parametre = MapSqlParameterSource()
            .addValue("orgnr", virksomhet.orgnr.verdi)
            .addValue("årstall", virksomhet.årstallOgKvartal.årstall)
            .addValue("kvartal", virksomhet.årstallOgKvartal.kvartal)
            .addValue("opprettet", virksomhet.opprettet)
        return jdbcTemplate.update(
            "insert into virksomheter_bekreftet_eksportert (orgnr, arstall, kvartal, opprettet) "
                    + "values (:orgnr, :årstall, :kvartal, :opprettet)",
            parametre
        )
    }

    private fun hentAlleVirksomhetEksportPerKvartal(): List<VirksomhetEksportPerKvartalMedDatoer> {
        return jdbcTemplate.query(
            "select orgnr, arstall, kvartal, eksportert, opprettet, oppdatert "
                    + "from eksport_per_kvartal ",
            MapSqlParameterSource()
        ) { resultSet: ResultSet, _: Int ->
            VirksomhetEksportPerKvartalMedDatoer(
                Orgnr(resultSet.getString("orgnr")),
                ÅrstallOgKvartal(resultSet.getInt("arstall"), resultSet.getInt("kvartal")),
                "true".equals(resultSet.getString("eksportert"), ignoreCase = true),
                if (resultSet.getTimestamp("oppdatert") != null) resultSet.getTimestamp("oppdatert")
                    .toLocalDateTime() else null
            )
        }
    }

    private fun hentAlleVirksomhetBekreftetEksportert(): List<VirksomhetBekreftetEksportert> {
        return jdbcTemplate.query(
            "select orgnr, arstall, kvartal, opprettet " + "from virksomheter_bekreftet_eksportert ",
            MapSqlParameterSource()
        ) { resultSet: ResultSet, _: Int ->
            VirksomhetBekreftetEksportert(
                Orgnr(resultSet.getString("orgnr")),
                ÅrstallOgKvartal(resultSet.getInt("arstall"), resultSet.getInt("kvartal")),
                resultSet.getTimestamp("opprettet").toLocalDateTime()
            )
        }
    }

    internal inner class VirksomhetEksportPerKvartalMedDatoer(
        var orgnr: Orgnr,
        var årstallOgKvartal: ÅrstallOgKvartal,
        var eksportert: Boolean,
        var oppdatert: LocalDateTime?
    )

    internal inner class VirksomhetBekreftetEksportert(
        var orgnr: Orgnr, var årstallOgKvartal: ÅrstallOgKvartal, var opprettet: LocalDateTime
    )

    companion object {
        val ORGNR_1 = Orgnr(ORGNR_VIRKSOMHET_1)
        val ORGNR_2 = Orgnr(ORGNR_VIRKSOMHET_2)
        val ORGNR_3 = Orgnr(ORGNR_VIRKSOMHET_3)
        val _2021_1 = ÅrstallOgKvartal(2021, 1)
    }


    fun opprettTestVirksomhetMetaData(
        jdbcTemplate: NamedParameterJdbcTemplate, årstall: Int, kvartal: Int, orgnr: String?
    ) {
        opprettTestVirksomhetMetaData(jdbcTemplate, årstall, kvartal, orgnr, false)
    }


    fun opprettTestVirksomhetMetaData(
        jdbcTemplate: NamedParameterJdbcTemplate,
        årstall: Int,
        kvartal: Int,
        orgnr: String?,
        eksportert: Boolean
    ): Int {
        val parametre: SqlParameterSource = MapSqlParameterSource()
            .addValue("orgnr", orgnr)
            .addValue("årstall", årstall)
            .addValue("kvartal", kvartal)
            .addValue("eksportert", eksportert)
        return jdbcTemplate.update(
            "insert into eksport_per_kvartal "
                    + "(orgnr, arstall, kvartal, eksportert) "
                    + "values "
                    + "(:orgnr, :årstall, :kvartal, :eksportert)",
            parametre
        )
    }
}
