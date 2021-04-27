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

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.ORGNR_VIRKSOMHET_1;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        int oppdaterteRader = eksporteringRepository.opprettEksport(
                Arrays.asList(
                        new VirksomhetEksportPerKvartal(
                                new Orgnr("999999998"),
                                new ÅrstallOgKvartal(2019, 2),
                                false
                        ),
                        new VirksomhetEksportPerKvartal(
                                new Orgnr("999999998"),
                                new ÅrstallOgKvartal(2019, 2),
                                false
                        )
                ));
        assertEquals(2, oppdaterteRader);

        List<VirksomhetEksportPerKvartal> resultat =
                eksporteringRepository.hentVirksomhetEksportPerKvartal(new ÅrstallOgKvartal(2019, 2));

        assertTrue(resultat.stream().anyMatch(virksomhetEksportPerKvartal ->
                virksomhetEksportPerKvartal.getOrgnr().equals("999999998") &&
                        !virksomhetEksportPerKvartal.eksportert() &&
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

        int antallOppdatert = eksporteringRepository.oppdaterTilEksportert(virksomhetTilEksport);

        assertEquals(1, antallOppdatert);
        List<VirksomhetEksportPerKvartalMedDatoer> results = hentAlleVirksomhetEksportPerKvartal();
        assertEquals(1, results.size());
        VirksomhetEksportPerKvartalMedDatoer actual = results.get(0);
        assertEquals(true, actual.eksportert);
        assertEquals(true, actual.oppdatert.isAfter(testStartDato));
    }

    private int opprettTestVirksomhetMetaData(int årstall, int kvartal, String orgnr) {
        SqlParameterSource parametre =
                new MapSqlParameterSource()
                        .addValue("orgnr", orgnr)
                        .addValue("årstall", årstall)
                        .addValue("kvartal", kvartal);
        return jdbcTemplate.update(
                "insert into eksport_per_kvartal " +
                        "(orgnr, arstall, kvartal) " +
                        "values " +
                        "(:orgnr, :årstall, :kvartal)",
                parametre
        );
    }

    public List<VirksomhetEksportPerKvartalMedDatoer> hentAlleVirksomhetEksportPerKvartal() {
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
