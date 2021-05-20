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
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.*;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("db-test")
@DataJdbcTest
class EksporteringRepositoryTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private EksporteringRepository eksporteringRepository;

    @BeforeEach
    void setUp() {
        eksporteringRepository = new EksporteringRepository(jdbcTemplate);
        slettAllStatistikkFraDatabase(jdbcTemplate);

    }

    @AfterEach
    void tearDown() {
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }


    @Test
    void hentVirksomhetEksportPerKvartal__returnerer_antall_VirksomhetEksportPerKvartal_funnet() {
        createVirksomhetEksportPerKvartal(new VirksomhetEksportPerKvartalMedDatoer(
                new Orgnr("999999999"),
                new ÅrstallOgKvartal(2019, 2),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));
        createVirksomhetEksportPerKvartal(new VirksomhetEksportPerKvartalMedDatoer(
                new Orgnr("999999998"),
                new ÅrstallOgKvartal(2019, 2),
                false,
                LocalDateTime.now(),
                null
        ));
        createVirksomhetEksportPerKvartal(new VirksomhetEksportPerKvartalMedDatoer(
                new Orgnr("999999998"),
                new ÅrstallOgKvartal(2019, 3),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        List<VirksomhetEksportPerKvartal> resultat =
                eksporteringRepository.hentVirksomhetEksportPerKvartal(new ÅrstallOgKvartal(2019, 2));

        assertEquals(2, resultat.size());
        assertTrue(resultat.stream().anyMatch(virksomhetEksportPerKvartal ->
                virksomhetEksportPerKvartal.getOrgnr().equals("999999998") &&
                        !virksomhetEksportPerKvartal.eksportert() &&
                        virksomhetEksportPerKvartal.getÅrstallOgKvartal().getÅrstall() == 2019 &&
                        virksomhetEksportPerKvartal.getÅrstallOgKvartal().getKvartal() == 2
        ));
        assertTrue(resultat.stream().anyMatch(virksomhetEksportPerKvartal ->
                virksomhetEksportPerKvartal.getOrgnr().equals("999999999") &&
                        virksomhetEksportPerKvartal.eksportert() &&
                        virksomhetEksportPerKvartal.getÅrstallOgKvartal().getÅrstall() == 2019 &&
                        virksomhetEksportPerKvartal.getÅrstallOgKvartal().getKvartal() == 2
        ));

    }

    @Test
    void oppdaterOgSetErEksportertTilTrue__skal_returnere_0_hvis_data_ikke_finnes() {
        int oppdaterteRader = eksporteringRepository.opprettEksport(null);
        assertEquals(0, oppdaterteRader);

        oppdaterteRader = eksporteringRepository.opprettEksport(Collections.emptyList());
        assertEquals(0, oppdaterteRader);
    }

    @Test
    void oppdater_med_oppdatert_dato() {
        LocalDateTime testStartDato = LocalDateTime.now();
        VirksomhetEksportPerKvartal virksomhetTilEksport = new VirksomhetEksportPerKvartal(
                new Orgnr(ORGNR_VIRKSOMHET_1),
                new ÅrstallOgKvartal(2020, 2),
                false
        );
        opprettTestVirksomhetMetaData(2020, 2, ORGNR_VIRKSOMHET_1);
        List<VirksomhetEksportPerKvartalMedDatoer> resultsBefore = hentAlleVirksomhetEksportPerKvartal();
        assertEquals(false, resultsBefore.get(0).eksportert);

        eksporteringRepository.oppdaterTilEksportert(virksomhetTilEksport);

        List<VirksomhetEksportPerKvartalMedDatoer> results = hentAlleVirksomhetEksportPerKvartal();
        assertEquals(1, results.size());
        VirksomhetEksportPerKvartalMedDatoer actual = results.get(0);
        assertEquals(true, actual.eksportert);
        assertEquals(true, actual.oppdatert.isAfter(testStartDato));
    }

    @Test
    void batchOppdater_med_oppdatert_dato() {
        LocalDateTime testStartDato = LocalDateTime.now();
        List<String> virksomheterSomSkalOppdateres = new ArrayList<>();
        virksomheterSomSkalOppdateres.add(ORGNR_VIRKSOMHET_1);
        virksomheterSomSkalOppdateres.add(ORGNR_VIRKSOMHET_2);
        opprettTestVirksomhetMetaData(2020, 2, ORGNR_VIRKSOMHET_1);
        opprettTestVirksomhetMetaData(2020, 2, ORGNR_VIRKSOMHET_2);
        opprettTestVirksomhetMetaData(2020, 2, ORGNR_VIRKSOMHET_3);
        List<VirksomhetEksportPerKvartalMedDatoer> resultsBefore = hentAlleVirksomhetEksportPerKvartal();
        assertEquals(false, resultsBefore.get(0).eksportert);
        assertEquals(false, resultsBefore.get(1).eksportert);
        assertEquals(false, resultsBefore.get(2).eksportert);

        eksporteringRepository.batchOppdaterTilEksportert(
                virksomheterSomSkalOppdateres,
                new ÅrstallOgKvartal(2020, 2)
        );

        List<VirksomhetEksportPerKvartalMedDatoer> results = hentAlleVirksomhetEksportPerKvartal();
        assertEquals(3, results.size());
        assertVirksomhetEksportPerKvartal(results, ORGNR_VIRKSOMHET_1, true, testStartDato);
        assertVirksomhetEksportPerKvartal(results, ORGNR_VIRKSOMHET_2, true, testStartDato);
        assertVirksomhetEksportPerKvartal(results, ORGNR_VIRKSOMHET_3, false, null);
    }

    @Test
    void hentAntallIkkeEksportertRader__skal_retunere_riktig_tall() {

        opprettTestVirksomhetMetaData(2020, 2, ORGNR_VIRKSOMHET_1);
        opprettTestVirksomhetMetaData(2020, 2, ORGNR_VIRKSOMHET_2);
        opprettTestVirksomhetMetaData(2020, 2, ORGNR_VIRKSOMHET_3, true);

        int antallIkkeFerdigEksportert = eksporteringRepository.hentAntallIkkeFerdigEksportert();
        assertEquals(2, antallIkkeFerdigEksportert);
    }

    @Test
    void slettEksportertPerKvartal__skal_slette_alt() {

        opprettTestVirksomhetMetaData(2020, 2, ORGNR_VIRKSOMHET_1);
        opprettTestVirksomhetMetaData(2020, 2, ORGNR_VIRKSOMHET_2);
        opprettTestVirksomhetMetaData(2020, 2, ORGNR_VIRKSOMHET_3, true);

        int antallSlettet = eksporteringRepository.slettEksportertPerKvartal();
        assertEquals(3, antallSlettet);
        List<VirksomhetEksportPerKvartalMedDatoer> results = hentAlleVirksomhetEksportPerKvartal();
        assertEquals(0, results.size());

    }

    private void assertVirksomhetEksportPerKvartal(
            List<VirksomhetEksportPerKvartalMedDatoer> results,
            String orgnr,
            boolean expectedEksportert,
            LocalDateTime oppdatertEtterDato
    ) {
        VirksomhetEksportPerKvartalMedDatoer actual = results
                .stream()
                .filter(
                        v -> v.orgnr.getVerdi().equals(orgnr)
                )
                .findFirst()
                .get();
        assertEquals(expectedEksportert, actual.eksportert);

        if (expectedEksportert) {
            assertEquals(true, actual.oppdatert.isAfter(oppdatertEtterDato));
        } else {
            assertNull(actual.oppdatert);
        }

    }

    private void opprettTestVirksomhetMetaData(int årstall, int kvartal, String orgnr) {
        opprettTestVirksomhetMetaData(årstall, kvartal, orgnr, false);
    }

    private int opprettTestVirksomhetMetaData(int årstall, int kvartal, String orgnr, boolean eksportert) {
        SqlParameterSource parametre =
                new MapSqlParameterSource()
                        .addValue("orgnr", orgnr)
                        .addValue("årstall", årstall)
                        .addValue("kvartal", kvartal)
                        .addValue("eksportert", eksportert);
        return jdbcTemplate.update(
                "insert into eksport_per_kvartal " +
                        "(orgnr, arstall, kvartal, eksportert) " +
                        "values " +
                        "(:orgnr, :årstall, :kvartal, :eksportert)",
                parametre
        );
    }

    private int createVirksomhetEksportPerKvartal(VirksomhetEksportPerKvartalMedDatoer virksomhet) {
        MapSqlParameterSource parametre = new MapSqlParameterSource()
                .addValue("orgnr", virksomhet.orgnr.getVerdi())
                .addValue("årstall", virksomhet.årstallOgKvartal.getÅrstall())
                .addValue("kvartal", virksomhet.årstallOgKvartal.getKvartal())
                .addValue("eksportert", virksomhet.eksportert)
                .addValue("oppdatert", virksomhet.oppdatert);

        return jdbcTemplate.update(
                "insert into eksport_per_kvartal (orgnr, arstall, kvartal, eksportert, oppdatert) " +
                        "values (:orgnr, :årstall, :kvartal, :eksportert, :oppdatert)",
                parametre);
    }

    private List<VirksomhetEksportPerKvartalMedDatoer> hentAlleVirksomhetEksportPerKvartal() {
        return jdbcTemplate.query(
                "select orgnr, arstall, kvartal, eksportert, opprettet, oppdatert " +
                        "from eksport_per_kvartal ",
                new MapSqlParameterSource(),
                (resultSet, rowNum) ->
                        new VirksomhetEksportPerKvartalMedDatoer(
                                new Orgnr(resultSet.getString("orgnr")),
                                new ÅrstallOgKvartal(
                                        resultSet.getInt("arstall"),
                                        resultSet.getInt("kvartal")
                                ),
                                "true".equalsIgnoreCase(resultSet.getString("eksportert")),
                                resultSet.getTimestamp("opprettet").toLocalDateTime(),
                                resultSet.getTimestamp("oppdatert") != null ?
                                        resultSet.getTimestamp("oppdatert").toLocalDateTime()
                                        : null
                        )
        );
    }

    class VirksomhetEksportPerKvartalMedDatoer {
        Orgnr orgnr;
        ÅrstallOgKvartal årstallOgKvartal;
        boolean eksportert;
        LocalDateTime opprettet;
        LocalDateTime oppdatert;

        public VirksomhetEksportPerKvartalMedDatoer(
                Orgnr orgnr,
                ÅrstallOgKvartal årstallOgKvartal,
                boolean eksportert,
                LocalDateTime opprettet,
                LocalDateTime oppdatert
        ) {
            this.orgnr = orgnr;
            this.årstallOgKvartal = årstallOgKvartal;
            this.eksportert = eksportert;
            this.opprettet = opprettet;
            this.oppdatert = oppdatert;
        }
    }

}
