package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet;
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

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.NÆRINGSKODE_2SIFFER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.NÆRINGSKODE_5SIFFER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.ORGNR_VIRKSOMHET_1;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.ORGNR_VIRKSOMHET_2;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.SEKTOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
    public void hentSektor_returnerer_eksisterende_Sektor() {
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

    private static void cleanUpTestDb(NamedParameterJdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("delete from virksomhet_metadata_til_eksportering", new MapSqlParameterSource());
    }


}