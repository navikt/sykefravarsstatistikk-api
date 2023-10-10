package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.NÆRINGSKODE_2SIFFER
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.NÆRINGSKODE_5SIFFER
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.ORGNR_VIRKSOMHET_1
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.ORGNR_VIRKSOMHET_2
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.ORGNR_VIRKSOMHET_3
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Sektor.Companion.fraSektorkode
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.VirksomhetMetadataRepository
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.datavarehus.DatavarehusRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.sql.ResultSet
import java.util.*

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AppConfigForJdbcTesterConfig::class])
@DataJdbcTest(excludeAutoConfiguration = [TestDatabaseAutoConfiguration::class])
open class VirksomhetMetadataRepositoryJdbcTest {
    @Autowired
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate? = null
    private var repository: VirksomhetMetadataRepository? = null
    @BeforeEach
    fun setUp() {
        repository = VirksomhetMetadataRepository(namedParameterJdbcTemplate!!)
        cleanUpTestDb(namedParameterJdbcTemplate)
    }

    @AfterEach
    fun tearDown() {
        cleanUpTestDb(namedParameterJdbcTemplate)
    }

    @Test
    fun slettNæringOgNæringskode5siffer__sletter_VirksomhetMetaData() {
        opprettTestVirksomhetMetaData(2020, 3)
        val antallSlettet = repository!!.slettNæringOgNæringskode5siffer()
        Assertions.assertThat(antallSlettet).isEqualTo(2)
    }

    @Test
    fun slettVirksomhetMetadata__sletter_VirksomhetMetaData() {
        opprettTestVirksomhetMetaData(2020, 3)
        val antallSlettet = repository!!.slettVirksomhetMetadata()
        Assertions.assertThat(antallSlettet).isEqualTo(3)
    }

    @Test
    fun opprettVirksomhetMetadataNæringskode5siffer__oppretter_riktig_metadataNæringskode5siffer() {
        val virksomhetMetadataMedNæringskode1 = VirksomhetMetadataMedNæringskode(
            Orgnr(ORGNR_VIRKSOMHET_1),
            ÅrstallOgKvartal(2020, 3),
            Næringskode("10001")
        )
        val virksomhetMetadataMedNæringskode2 = VirksomhetMetadataMedNæringskode(
            Orgnr(ORGNR_VIRKSOMHET_1),
            ÅrstallOgKvartal(2020, 3),
            Næringskode("10002")
        )
        repository!!.opprettVirksomhetMetadataNæringskode5siffer(
            Arrays.asList(
                virksomhetMetadataMedNæringskode1, virksomhetMetadataMedNæringskode2
            )
        )
        val results = hentAlleVirksomhetMetadataNæringskode5siffer(namedParameterJdbcTemplate)
        Assertions.assertThat(results[0]).isEqualTo(virksomhetMetadataMedNæringskode1)
        Assertions.assertThat(results[1]).isEqualTo(virksomhetMetadataMedNæringskode2)
    }

    @Test
    fun opprettVirksomhetMetadata__oppretter_riktig_metadata() {
        val virksomhetMetadataVirksomhet1 = VirksomhetMetadata(
            Orgnr(ORGNR_VIRKSOMHET_1),
            "Virksomhet 1",
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            Sektor.PRIVAT,
            NÆRINGSKODE_2SIFFER,
            NÆRINGSKODE_5SIFFER,
            ÅrstallOgKvartal(2020, 3)
        )
        val virksomhetMetadataVirksomhet2 = VirksomhetMetadata(
            Orgnr(ORGNR_VIRKSOMHET_2),
            "Virksomhet 2",
            DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET,
            Sektor.PRIVAT,
            NÆRINGSKODE_2SIFFER,
            NÆRINGSKODE_5SIFFER,
            ÅrstallOgKvartal(2020, 3)
        )
        repository!!.opprettVirksomhetMetadata(
            Arrays.asList(virksomhetMetadataVirksomhet1, virksomhetMetadataVirksomhet2)
        )
        val results = hentAlleVirksomhetMetadata(namedParameterJdbcTemplate)
        Assertions.assertThat(results[0]).isEqualTo(virksomhetMetadataVirksomhet1)
        Assertions.assertThat(results[1]).isEqualTo(virksomhetMetadataVirksomhet2)
    }

    @Test
    fun hentVirksomhetMetadata_returnerer_riktige_metdata_for_en_gitt_årstall_og_kvartal() {
        opprettTestVirksomhetMetaData(2020, 2)
        val results = repository!!.hentVirksomhetMetadataMedNæringskoder(ÅrstallOgKvartal(2019, 2))
        Assertions.assertThat(results.size).isEqualTo(0)
    }

    @Test
    fun hentVirksomhetMetadata_returnerer_riktige_metdata() {
        opprettTestVirksomhetMetaData(2020, 3)
        val results = repository!!.hentVirksomhetMetadataMedNæringskoder(ÅrstallOgKvartal(2020, 3))
        Assertions.assertThat(results.size).isEqualTo(3)
        val virksomhetMetadataVirksomhet1 =
            results.stream().filter { r: VirksomhetMetadata -> ORGNR_VIRKSOMHET_1 == r.orgnr }
                .findFirst().get()
        val næringskoder: List<Næringskode> = virksomhetMetadataVirksomhet1.næringOgNæringskode5siffer
        Assertions.assertThat(næringskoder.contains(Næringskode("71001")))
            .isTrue()
        Assertions.assertThat(næringskoder.contains(Næringskode("71002")))
            .isTrue()
    }

    private fun hentAlleVirksomhetMetadata(
        jdbcTemplate: NamedParameterJdbcTemplate?
    ): List<VirksomhetMetadata> {
        return jdbcTemplate!!.query(
            "select * from virksomhet_metadata",
            MapSqlParameterSource()
        ) { rs: ResultSet, rowNum: Int ->
            VirksomhetMetadata(
                Orgnr(rs.getString("orgnr")),
                rs.getString("navn"),
                rs.getString("rectype"),
                fraSektorkode(rs.getString("sektor"))!!,
                rs.getString("primarnaring"),
                rs.getString("primarnaringskode"),
                ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal"))
            )
        }
    }

    private fun hentAlleVirksomhetMetadataNæringskode5siffer(
        jdbcTemplate: NamedParameterJdbcTemplate?
    ): List<VirksomhetMetadataMedNæringskode> {
        return jdbcTemplate!!.query(
            "select * from virksomhet_metadata_naring_kode_5siffer",
            MapSqlParameterSource()
        ) { rs: ResultSet, rowNum: Int ->
            VirksomhetMetadataMedNæringskode(
                Orgnr(rs.getString("orgnr")),
                ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
                Næringskode(rs.getString("naring_kode_5siffer"))
            )
        }
    }

    private fun opprettTestVirksomhetMetaData(årstall: Int, kvartal: Int): Int {
        namedParameterJdbcTemplate!!.update(
            "insert into virksomhet_metadata (orgnr, navn, rectype, sektor, primarnaring, primarnaringskode, arstall, kvartal) "
                    + "VALUES (:orgnr, :navn, :rectype, :sektor, :primarnaring, :primarnaringskode, :årstall, :kvartal)",
            parametreViksomhetMetadata(
                ORGNR_VIRKSOMHET_1, "Virksomhet 1", "2", "3", "71", "71000", årstall, kvartal
            )
        )
        namedParameterJdbcTemplate.update(
            "insert into virksomhet_metadata_naring_kode_5siffer (orgnr, naring_kode, naring_kode_5siffer, arstall, kvartal) "
                    + "VALUES (:orgnr, :naring_kode, :naring_kode_5siffer, :årstall, :kvartal)",
            parametreViksomhetMetadataNæring5Siffer(
                ORGNR_VIRKSOMHET_1, "71", "71001", årstall, kvartal
            )
        )
        namedParameterJdbcTemplate.update(
            "insert into virksomhet_metadata_naring_kode_5siffer (orgnr, naring_kode, naring_kode_5siffer, arstall, kvartal) "
                    + "VALUES (:orgnr, :naring_kode, :naring_kode_5siffer, :årstall, :kvartal)",
            parametreViksomhetMetadataNæring5Siffer(
                ORGNR_VIRKSOMHET_1, "71", "71002", årstall, kvartal
            )
        )
        namedParameterJdbcTemplate.update(
            "insert into virksomhet_metadata (orgnr, navn, rectype, sektor, primarnaring, primarnaringskode, arstall, kvartal) "
                    + "VALUES (:orgnr, :navn, :rectype, :sektor, :primarnaring, :primarnaringskode, :årstall, :kvartal)",
            parametreViksomhetMetadata(
                ORGNR_VIRKSOMHET_2, "Virksomhet 2", "2", "3", "10", "10000", årstall, kvartal
            )
        )
        namedParameterJdbcTemplate.update(
            "insert into virksomhet_metadata (orgnr, navn, rectype, sektor, primarnaring, primarnaringskode, arstall, kvartal) "
                    + "VALUES (:orgnr, :navn, :rectype, :sektor, :primarnaring, :primarnaringskode, :årstall, :kvartal)",
            parametreViksomhetMetadata(
                ORGNR_VIRKSOMHET_3, "Virksomhet 3", "2", "3", "10", "10000", årstall, kvartal
            )
        )
        return 0
    }

    private fun parametreViksomhetMetadata(
        orgnr: String,
        navn: String,
        rectype: String,
        sektor: String,
        primarnaring: String,
        primarnaringskode: String,
        årstall: Int,
        kvartal: Int
    ): MapSqlParameterSource {
        return MapSqlParameterSource()
            .addValue("orgnr", orgnr)
            .addValue("navn", navn)
            .addValue("rectype", rectype)
            .addValue("sektor", sektor)
            .addValue("primarnaring", primarnaring)
            .addValue("primarnaringskode", primarnaringskode)
            .addValue("årstall", årstall)
            .addValue("kvartal", kvartal)
    }

    private fun parametreViksomhetMetadataNæring5Siffer(
        orgnr: String,
        næringskode2Siffer: String,
        næringskode5Siffer: String,
        årstall: Int,
        kvartal: Int
    ): MapSqlParameterSource {
        return MapSqlParameterSource()
            .addValue("orgnr", orgnr)
            .addValue("naring_kode", næringskode2Siffer)
            .addValue("naring_kode_5siffer", næringskode5Siffer)
            .addValue("årstall", årstall)
            .addValue("kvartal", kvartal)
    }

    companion object {
        private fun cleanUpTestDb(jdbcTemplate: NamedParameterJdbcTemplate?) {
            jdbcTemplate!!.update("delete from virksomhet_metadata", MapSqlParameterSource())
        }
    }
}
