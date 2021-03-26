package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.SykefraværsstatistikkNæring;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.slettAllStatistikkFraDatabase;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("db-test")
@DataJdbcTest
class AlleNaringRepositoryTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private AlleNaringRepository alleNæringRepository;

    @BeforeEach
    void setUp() {
        slettAllStatistikkFraDatabase(jdbcTemplate);
        alleNæringRepository = new AlleNaringRepository(jdbcTemplate);
        Næring produksjonAvKlær = new Næring("14", "Produksjon");

        jdbcTemplate.update(
                "insert into sykefravar_statistikk_naring (naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(produksjonAvKlær, 2019, 2, 10, new BigDecimal(2), new BigDecimal(100))
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_naring (naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(produksjonAvKlær, 2019, 1, 10, new BigDecimal(3), new BigDecimal(100))
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_naring (naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(new Næring("86", "Utdanning"), 2019, 2, 10, new BigDecimal(5), new BigDecimal(100))
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_naring (naring_kode, arstall, kvartal, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:naring_kode, :arstall, :kvartal, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(new Næring("86", "Utdanning"), 2018, 4, 10, new BigDecimal("7.5"), new BigDecimal(100))
        );

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void hentSykefraværprosentAlleNæringerForEttKvartal__skal_hente_alle_næringer_for_ett_kvartal() {
        List<SykefraværsstatistikkNæring> resultat = alleNæringRepository.hentSykefraværprosentAlleNæringerForEttKvartal(new ÅrstallOgKvartal(2019, 2));

        assertThat(resultat.size()).isEqualTo(2);

    }

    @Test
    void hentSykefraværprosentAlleNæringerForEttKvartal__skalMaskereVirksomheterUnderMinimumAntallPersoner() {

        List<SykefraværsstatistikkNæring> resultat = alleNæringRepository.hentSykefraværprosentAlleNæringerForEttKvartal(new ÅrstallOgKvartal(2019, 2));

        assertTrue(resultat.stream().anyMatch(
                sykefraværsstatistikkNæring ->
                        sykefraværsstatistikkNæring.getTapteDagsverk().compareTo(new BigDecimal(2)) == 0 && sykefraværsstatistikkNæring.getNæringkode().equals("14")
        ));

    }

    @Test
    void hentSykefraværprosentAlleNæringerForEttKvartal__skal_returnere_data_for_riktig_ÅrstallOgKvartal() {
        List<SykefraværsstatistikkNæring> resultat = alleNæringRepository.hentSykefraværprosentAlleNæringerForEttKvartal(new ÅrstallOgKvartal(2019, 2));

        assertTrue(resultat.stream().allMatch(
                sykefraværsstatistikkNæring ->
                        sykefraværsstatistikkNæring.getÅrstall() == 2019 && sykefraværsstatistikkNæring.getKvartal() == 2
        ));
    }

    @Test
    void hentSykefraværprosentAlleNæringerForEttKvartal__skal_maskere_data_for_næring() {
        List<SykefraværsstatistikkNæring> resultat = alleNæringRepository.hentSykefraværprosentAlleNæringerForEttKvartal(new ÅrstallOgKvartal(2018, 4));

        assertTrue(resultat.stream().anyMatch(sykefraværsstatistikkNæring ->
                sykefraværsstatistikkNæring.getNæringkode().equals("86") &&
                        sykefraværsstatistikkNæring.getÅrstall() == 2018 &&
                        sykefraværsstatistikkNæring.getKvartal() == 4 &&
                        sykefraværsstatistikkNæring.getTapteDagsverk().compareTo(new BigDecimal("7.5")) == 0 &&
                        sykefraværsstatistikkNæring.getMuligeDagsverk().compareTo(new BigDecimal(100)) == 0
        ));
    }

    @Test
    void  hentSykefraværprosentAlleNæringerForEttKvartal__skal_returnere_riktig_data_til_alle_næringer() {
        List<SykefraværsstatistikkNæring> resultat = alleNæringRepository.hentSykefraværprosentAlleNæringerForEttKvartal(new ÅrstallOgKvartal(2019, 2));

        assertTrue(resultat.stream().anyMatch(sykefraværsstatistikkNæring ->
                sykefraværsstatistikkNæring.getNæringkode().equals("14") &&
                        sykefraværsstatistikkNæring.getÅrstall() == 2019 &&
                        sykefraværsstatistikkNæring.getKvartal() == 2 &&
                        sykefraværsstatistikkNæring.getTapteDagsverk().compareTo(new BigDecimal(2)) == 0 &&
                        sykefraværsstatistikkNæring.getMuligeDagsverk().compareTo(new BigDecimal(100)) == 0
        ));
        assertTrue(resultat.stream().anyMatch(sykefraværsstatistikkNæring ->
                sykefraværsstatistikkNæring.getNæringkode().equals("86") &&
                        sykefraværsstatistikkNæring.getÅrstall() == 2019 &&
                        sykefraværsstatistikkNæring.getKvartal() == 2 &&
                        sykefraværsstatistikkNæring.getTapteDagsverk().compareTo(new BigDecimal(5)) == 0 &&
                        sykefraværsstatistikkNæring.getMuligeDagsverk().compareTo(new BigDecimal(100)) == 0
        ));
    }
/*
    @Test
    void oppdaterOgSetErEksportertTilTrue__skal_returnere_1() {
        int oppdaterteRader = alleVirksomheterRepository.oppdaterOgSetErEksportertTilTrue(
                "sykefravar_statistikk_virksomhet_med_gradering",
                new Orgnr("999999998"),
                new ÅrstallOgKvartal(2019, 2));

        assertEquals(1, oppdaterteRader);
    }

    @Test
    void oppdaterOgSetErEksportertTilTrue__skal_returnere_0_hvis_data_ikke_finnes() {
        int oppdaterteRader = alleVirksomheterRepository.oppdaterOgSetErEksportertTilTrue(
                "sykefravar_statistikk_virksomhet_med_gradering",
                new Orgnr("999999997"),
                new ÅrstallOgKvartal(2019, 2));

        assertEquals(0, oppdaterteRader);
    }


    */

    private MapSqlParameterSource parametre(Næring næring, int årstall, int kvartal, int antallPersoner, BigDecimal tapteDagsverk, BigDecimal muligeDagsverk) {
        return new MapSqlParameterSource()
                .addValue("naring_kode", næring.getKode())
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("antall_personer", antallPersoner)
                .addValue("tapte_dagsverk", tapteDagsverk)
                .addValue("mulige_dagsverk", muligeDagsverk);
    }
}
