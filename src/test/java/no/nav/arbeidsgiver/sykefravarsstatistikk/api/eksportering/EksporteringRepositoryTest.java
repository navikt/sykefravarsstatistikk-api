package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

}
