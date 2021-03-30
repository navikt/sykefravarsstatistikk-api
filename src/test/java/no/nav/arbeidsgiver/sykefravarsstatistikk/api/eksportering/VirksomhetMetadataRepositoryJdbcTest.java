package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("db-test")
@DataJdbcTest
class VirksomhetMetadataRepositoryJdbcTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private VirksomhetMetadataRepository repository;

    @BeforeEach
    public void setUp() {
        repository = new VirksomhetMetadataRepository(namedParameterJdbcTemplate);
        cleanUpTestDb(namedParameterJdbcTemplate);
    }

    @AfterEach
    public void tearDown() {
        cleanUpTestDb(namedParameterJdbcTemplate);
    }


    @Test
    public void opprettVirksomhetMetadata__oppretter_riktig_metadata() {
        VirksomhetMetadata virksomhetMetadataVirksomhet1 = new VirksomhetMetadata(
                new Orgnr(ORGNR_VIRKSOMHET_1),
                new ÅrstallOgKvartal(2020, 3),
                SEKTOR,
                NÆRINGSKODE_5SIFFER,
                NÆRINGSKODE_2SIFFER,
                false
        );
        VirksomhetMetadata virksomhetMetadataVirksomhet2 = new VirksomhetMetadata(
                new Orgnr(ORGNR_VIRKSOMHET_2),
                new ÅrstallOgKvartal(2020, 3),
                SEKTOR,
                NÆRINGSKODE_5SIFFER,
                NÆRINGSKODE_2SIFFER,
                false
        );

        repository.opprettVirksomhetMetadata(Arrays.asList(
                virksomhetMetadataVirksomhet1,
                virksomhetMetadataVirksomhet2
        ));

        List<VirksomhetMetadata> results = hentAlleVirksomhetMetadata(namedParameterJdbcTemplate);
        assertThat(results.get(0)).isEqualTo(virksomhetMetadataVirksomhet1);
        assertThat(results.get(1)).isEqualTo(virksomhetMetadataVirksomhet2);
    }

    @Test
    public void hentVirksomhetMetadata_returnerer_riktige_metdata() {
        opprettTestVirksomhetMetaData();

        List<VirksomhetMetadata> results = repository.hentVirksomhetMetadata (new ÅrstallOgKvartal(2019,2));

        assertThat(results.size()).isEqualTo(2);
        assertThat(results.get(0).årstallOgKvartal.getÅrstall()).isEqualTo(2019);
        assertThat(results.get(0).årstallOgKvartal.getKvartal()).isEqualTo(2);
    }


    private List<VirksomhetMetadata> hentAlleVirksomhetMetadata(NamedParameterJdbcTemplate jdbcTemplate) {
        return jdbcTemplate.query(
                "select * from virksomhet_metadata_til_eksportering",
                new MapSqlParameterSource(),
                (rs, rowNum) -> new VirksomhetMetadata(
                        new Orgnr(rs.getString("orgnr")),
                        new ÅrstallOgKvartal(rs.getInt("arstall"), rs.getInt("kvartal")),
                        rs.getString("sektor"),
                        rs.getString("naring_kode"),
                        rs.getString("naring_kode_5siffer"),
                        rs.getString("eksportert").equals("true")
                )
        );
    }

    private int opprettTestVirksomhetMetaData() {
        namedParameterJdbcTemplate.update(
                "insert into virksomhet_metadata_til_eksportering (orgnr,arstall,kvartal,sektor,naring_kode,naring_kode_5siffer) "
                        + "VALUES (:orgnr, :arstall, :kvartal, :sektor, :naring_kode, :naring_kode_5siffer)",
                parametre(ORGNR_VIRKSOMHET_1, 2019, 2, "3", "71", "71215")
        );
        namedParameterJdbcTemplate.update(
                "insert into virksomhet_metadata_til_eksportering (orgnr,arstall,kvartal,sektor,naring_kode,naring_kode_5siffer) "
                        + "VALUES (:orgnr, :arstall, :kvartal, :sektor, :naring_kode, :naring_kode_5siffer)",
                parametre(ORGNR_VIRKSOMHET_2, 2020, 2, "3", "10", "41000")
        );
        namedParameterJdbcTemplate.update(
                "insert into virksomhet_metadata_til_eksportering (orgnr,arstall,kvartal,sektor,naring_kode,naring_kode_5siffer) "
                        + "VALUES (:orgnr, :arstall, :kvartal, :sektor, :naring_kode, :naring_kode_5siffer)",
                parametre(ORGNR_VIRKSOMHET_2, 2019, 2, "3", "10", "41000")
        );
        return 0;
    }

    private static void cleanUpTestDb(NamedParameterJdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("delete from virksomhet_metadata_til_eksportering", new MapSqlParameterSource());
    }

    private MapSqlParameterSource parametre(String orgnr, int årstall, int kvartal, String sektor, String næringskode2Siffer, String næringskode5Siffer) {
        return new MapSqlParameterSource()
                .addValue("orgnr", orgnr)
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("sektor", sektor)
                .addValue("naring_kode", næringskode2Siffer)
                .addValue("naring_kode_5siffer", næringskode5Siffer);
    }
}
