package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.AppConfigForJdbcTesterConfig;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Kvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.config.LocalOgUnitTestOidcConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.ORGNR_VIRKSOMHET_1;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.ORGNR_VIRKSOMHET_2;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.ORGNR_VIRKSOMHET_3;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllEksportDataFraDatabase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("db-test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfigForJdbcTesterConfig.class})
@DataJdbcTest(excludeAutoConfiguration = {TestDatabaseAutoConfiguration.class, LocalOgUnitTestOidcConfiguration.class})
class EksporteringRepositoryTest {

    public static final Orgnr ORGNR_1 = new Orgnr(ORGNR_VIRKSOMHET_1);
    public static final Orgnr ORGNR_2 = new Orgnr(ORGNR_VIRKSOMHET_2);
    public static final Orgnr ORGNR_3 = new Orgnr(ORGNR_VIRKSOMHET_3);
    public static final Kvartal _2021_1 = new Kvartal(2021, 1);

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private EksporteringRepository eksporteringRepository;

    @BeforeEach
    void setUp() {
        eksporteringRepository = new EksporteringRepository(jdbcTemplate);
        slettAllEksportDataFraDatabase(jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        slettAllEksportDataFraDatabase(jdbcTemplate);
    }


    @Test
    void hentVirksomhetEksportPerKvartal__returnerer_antall_VirksomhetEksportPerKvartal_funnet() {
        createVirksomhetEksportPerKvartal(new VirksomhetEksportPerKvartalMedDatoer(
                new Orgnr("999999999"),
                new Kvartal(2019, 2),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));
        createVirksomhetEksportPerKvartal(new VirksomhetEksportPerKvartalMedDatoer(
                new Orgnr("999999998"),
                new Kvartal(2019, 2),
                false,
                LocalDateTime.now(),
                null
        ));
        createVirksomhetEksportPerKvartal(new VirksomhetEksportPerKvartalMedDatoer(
                new Orgnr("999999998"),
                new Kvartal(2019, 3),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        List<VirksomhetEksportPerKvartal> resultat =
                eksporteringRepository.hentVirksomhetEksportPerKvartal(new Kvartal(2019, 2));

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
    void batchOpprettVirksomheterBekreftetEksportert__oppretter_ingenting_hvis_lista_er_tom() {
        List<String> virksomheterBekreftetEksportert = new ArrayList<>();

        eksporteringRepository.batchOpprettVirksomheterBekreftetEksportert(
                virksomheterBekreftetEksportert,
                new Kvartal(2020, 2)
        );

        List<VirksomhetBekreftetEksportert> results = hentAlleVirksomhetBekreftetEksportert();
        assertEquals(0, results.size());
    }

    @Test
    void batchOpprettVirksomheterBekreftetEksportert__opprett_i_batch() {
        List<String> virksomheterBekreftetEksportert = new ArrayList<>();
        virksomheterBekreftetEksportert.add(ORGNR_VIRKSOMHET_1);
        virksomheterBekreftetEksportert.add(ORGNR_VIRKSOMHET_2);
        virksomheterBekreftetEksportert.add(ORGNR_VIRKSOMHET_3);

        eksporteringRepository.batchOpprettVirksomheterBekreftetEksportert(
                virksomheterBekreftetEksportert,
                new Kvartal(2020, 2)
        );

        List<VirksomhetBekreftetEksportert> results = hentAlleVirksomhetBekreftetEksportert();
        assertEquals(3, results.size());
        assertVirksomhetBekreftetEksportert(results, ORGNR_VIRKSOMHET_1);
        assertVirksomhetBekreftetEksportert(results, ORGNR_VIRKSOMHET_2);
        assertVirksomhetBekreftetEksportert(results, ORGNR_VIRKSOMHET_3);
    }

    @Test
    void oppdaterVirksomheterIEksportTabell__oppdater_virksomheter_som_er_bekreftet_eksportert_og_returnerer_antall_oppdatert() {
        LocalDateTime testStartDato = LocalDateTime.now();
        createVirksomhetBekreftetEksportert(new VirksomhetBekreftetEksportert(ORGNR_1, _2021_1, testStartDato));
        createVirksomhetBekreftetEksportert(new VirksomhetBekreftetEksportert(ORGNR_2, _2021_1, testStartDato));

        createVirksomhetEksportPerKvartal(
                new VirksomhetEksportPerKvartalMedDatoer(ORGNR_1, _2021_1, true, testStartDato, testStartDato)
        );
        createVirksomhetEksportPerKvartal(
                new VirksomhetEksportPerKvartalMedDatoer(ORGNR_2, _2021_1, false, testStartDato, null)
        );
        createVirksomhetEksportPerKvartal(
                new VirksomhetEksportPerKvartalMedDatoer(ORGNR_3, _2021_1, false, testStartDato, null)
        );

        int antallOppdatert = eksporteringRepository.oppdaterAlleVirksomheterIEksportTabellSomErBekrreftetEksportert();

        assertEquals(1, antallOppdatert);
        List<VirksomhetEksportPerKvartalMedDatoer> results = hentAlleVirksomhetEksportPerKvartal();
        assertVirksomhetEksportPerKvartal(results, ORGNR_1.getVerdi(), true, testStartDato);
        assertVirksomhetEksportPerKvartal(results, ORGNR_2.getVerdi(), true, testStartDato, true);
        assertVirksomhetEksportPerKvartal(results, ORGNR_3.getVerdi(), false, testStartDato);
    }

    @Test
    void slettVirksomheterBekreftetEksportert__sletter_alle_rader_i_tabellen_og_returnerer_antall_slettet() {
        createVirksomhetBekreftetEksportert(
                new VirksomhetBekreftetEksportert(
                        new Orgnr(ORGNR_VIRKSOMHET_1),
                        new Kvartal(2020, 1),
                        LocalDateTime.now()
                )
        );
        createVirksomhetBekreftetEksportert(
                new VirksomhetBekreftetEksportert(
                        new Orgnr(ORGNR_VIRKSOMHET_2),
                        new Kvartal(2020, 1),
                        LocalDateTime.now()
                )
        );

        int antallSlettet = eksporteringRepository.slettVirksomheterBekreftetEksportert();

        assertEquals(2, antallSlettet);
        List<VirksomhetEksportPerKvartalMedDatoer> results = hentAlleVirksomhetEksportPerKvartal();
        assertEquals(0, hentAlleVirksomhetBekreftetEksportert().size());
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
            LocalDateTime oppdatertEtterDato) {

        assertVirksomhetEksportPerKvartal(results, orgnr, expectedEksportert, oppdatertEtterDato, false);
    }

    private void assertVirksomhetEksportPerKvartal(
            List<VirksomhetEksportPerKvartalMedDatoer> results,
            String orgnr,
            boolean expectedEksportert,
            LocalDateTime oppdatertEtterDato,
            boolean sjekkOppdatertDatoErEndret
    ) {
        VirksomhetEksportPerKvartalMedDatoer actual = results
                .stream()
                .filter(
                        v -> v.orgnr.getVerdi().equals(orgnr)
                )
                .findFirst()
                .get();
        assertEquals(expectedEksportert, actual.eksportert);

        if (!expectedEksportert) {
            assertNull(actual.oppdatert);
        }

        if (sjekkOppdatertDatoErEndret) {
            assertEquals(true, actual.oppdatert.isAfter(oppdatertEtterDato));
        }
    }

    private void assertVirksomhetBekreftetEksportert(
            List<VirksomhetBekreftetEksportert> results,
            String orgnr
    ) {
        VirksomhetBekreftetEksportert actual = results
                .stream()
                .filter(
                        v -> v.orgnr.getVerdi().equals(orgnr)
                )
                .findFirst()
                .get();
        assertEquals(orgnr, actual.orgnr.getVerdi());
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
                .addValue("årstall", virksomhet.kvartal.getÅrstall())
                .addValue("kvartal", virksomhet.kvartal.getKvartal())
                .addValue("eksportert", virksomhet.eksportert)
                .addValue("oppdatert", virksomhet.oppdatert);

        return jdbcTemplate.update(
                "insert into eksport_per_kvartal (orgnr, arstall, kvartal, eksportert, oppdatert) " +
                        "values (:orgnr, :årstall, :kvartal, :eksportert, :oppdatert)",
                parametre);
    }

    private int createVirksomhetBekreftetEksportert(VirksomhetBekreftetEksportert virksomhet) {
        MapSqlParameterSource parametre = new MapSqlParameterSource()
                .addValue("orgnr", virksomhet.orgnr.getVerdi())
                .addValue("årstall", virksomhet.kvartal.getÅrstall())
                .addValue("kvartal", virksomhet.kvartal.getKvartal())
                .addValue("opprettet", virksomhet.opprettet);

        return jdbcTemplate.update(
                "insert into virksomheter_bekreftet_eksportert (orgnr, arstall, kvartal, opprettet) " +
                        "values (:orgnr, :årstall, :kvartal, :opprettet)",
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
                                new Kvartal(
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

    private List<VirksomhetBekreftetEksportert> hentAlleVirksomhetBekreftetEksportert() {
        return jdbcTemplate.query(
                "select orgnr, arstall, kvartal, opprettet " +
                        "from virksomheter_bekreftet_eksportert ",
                new MapSqlParameterSource(),
                (resultSet, rowNum) ->
                        new VirksomhetBekreftetEksportert(
                                new Orgnr(resultSet.getString("orgnr")),
                                new Kvartal(
                                        resultSet.getInt("arstall"),
                                        resultSet.getInt("kvartal")
                                ),
                                resultSet.getTimestamp("opprettet").toLocalDateTime()
                        )
        );
    }


    class VirksomhetEksportPerKvartalMedDatoer {
        Orgnr orgnr;
        Kvartal kvartal;
        boolean eksportert;
        LocalDateTime opprettet;
        LocalDateTime oppdatert;

        public VirksomhetEksportPerKvartalMedDatoer(
                Orgnr orgnr,
                Kvartal kvartal,
                boolean eksportert,
                LocalDateTime opprettet,
                LocalDateTime oppdatert
        ) {
            this.orgnr = orgnr;
            this.kvartal = kvartal;
            this.eksportert = eksportert;
            this.opprettet = opprettet;
            this.oppdatert = oppdatert;
        }
    }

    class VirksomhetBekreftetEksportert {
        Orgnr orgnr;
        Kvartal kvartal;
        LocalDateTime opprettet;

        public VirksomhetBekreftetEksportert(
                Orgnr orgnr,
                Kvartal kvartal,
                LocalDateTime opprettet
        ) {
            this.orgnr = orgnr;
            this.kvartal = kvartal;
            this.opprettet = opprettet;
        }
    }

}
