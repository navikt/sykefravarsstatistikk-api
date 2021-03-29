package no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.AlleVirksomheterRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartalMedOrgNr;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("db-test")
@DataJdbcTest
class AlleVirksomheterRepositoryTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private AlleVirksomheterRepository alleVirksomheterRepository;

    @BeforeEach
    void setUp() {
        alleVirksomheterRepository = new AlleVirksomheterRepository(jdbcTemplate);
        slettAllStatistikkFraDatabase(jdbcTemplate);

        jdbcTemplate.update(
                "insert into sykefravar_statistikk_virksomhet_med_gradering (arstall, kvartal, orgnr, naring, naring_kode, antall_graderte_sykemeldinger, tapte_dagsverk_gradert_sykemelding,antall_sykemeldinger, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :orgnr, :naring, :naring_kode, :antall_graderte_sykemeldinger, :tapte_dagsverk_gradert_sykemelding, :antall_sykemeldinger, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(2019, 2, "999999999", "85", "08500", 0, 0, 0, 3, 4, 40)
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_virksomhet_med_gradering (arstall, kvartal, orgnr, naring, naring_kode, antall_graderte_sykemeldinger, tapte_dagsverk_gradert_sykemelding,antall_sykemeldinger, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :orgnr, :naring, :naring_kode, :antall_graderte_sykemeldinger, :tapte_dagsverk_gradert_sykemelding, :antall_sykemeldinger, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(2019, 2, "999999999", "85", "08500", 0, 0, 0, 3, 0, 60)
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_virksomhet_med_gradering (arstall, kvartal, orgnr, naring, naring_kode, antall_graderte_sykemeldinger, tapte_dagsverk_gradert_sykemelding,antall_sykemeldinger, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :orgnr, :naring, :naring_kode, :antall_graderte_sykemeldinger, :tapte_dagsverk_gradert_sykemelding, :antall_sykemeldinger, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(2019, 2, "999999998", "85", "08500", 0, 0, 0, 4, 0, 100)
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_virksomhet_med_gradering (arstall, kvartal, orgnr, naring, naring_kode, antall_graderte_sykemeldinger, tapte_dagsverk_gradert_sykemelding,antall_sykemeldinger, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :orgnr, :naring, :naring_kode, :antall_graderte_sykemeldinger, :tapte_dagsverk_gradert_sykemelding, :antall_sykemeldinger, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(2017, 4, "999999997", "85", "08500", 0, 0, 0, 40, 20, 115)
        );
    }

    @AfterEach
    void tearDown() {
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @Test
    void skalReturnereToRaderNårManHenterÅrstall2019Kvartal2() {
        List<SykefraværForEttKvartalMedOrgNr> resultat = alleVirksomheterRepository.hentSykefraværprosentAlleVirksomheterForEttKvartal(new ÅrstallOgKvartal(2019, 2));

        assertThat(resultat.size()).isEqualTo(2);

    }

    @Test
    void skalMaskereVirksomheterUnderMinimumAntallPersoner() {

        List<SykefraværForEttKvartalMedOrgNr> resultat = alleVirksomheterRepository.hentSykefraværprosentAlleVirksomheterForEttKvartal(new ÅrstallOgKvartal(2019, 2));

        assertTrue(resultat.stream().anyMatch(
                sykefraværForEttKvartalMedOrgNr ->
                        sykefraværForEttKvartalMedOrgNr.isErMaskert() && sykefraværForEttKvartalMedOrgNr.getOrgnr().equals("999999998")
        ));

    }

    @Test
    void SkalReturnereDataForRiktigÅrstallOgKvartal() {
        List<SykefraværForEttKvartalMedOrgNr> resultat = alleVirksomheterRepository.hentSykefraværprosentAlleVirksomheterForEttKvartal(new ÅrstallOgKvartal(2019, 2));

        assertTrue(resultat.stream().allMatch(
                sykefraværForEttKvartalMedOrgNr ->
                        sykefraværForEttKvartalMedOrgNr.getÅrstallOgKvartal().compareTo(new ÅrstallOgKvartal(2019, 2)) == 0
        ));
    }

    @Test
    void SkalMaskereDataNårVirksomhetErMaskert() {
        List<SykefraværForEttKvartalMedOrgNr> resultat = alleVirksomheterRepository.hentSykefraværprosentAlleVirksomheterForEttKvartal(new ÅrstallOgKvartal(2019, 2));

        assertTrue(resultat.stream().anyMatch(sykefraværForEttKvartalMedOrgNr ->
                sykefraværForEttKvartalMedOrgNr.getOrgnr().equals("999999998") &&
                        sykefraværForEttKvartalMedOrgNr.isErMaskert() &&
                        sykefraværForEttKvartalMedOrgNr.getProsent() == null &&
                        sykefraværForEttKvartalMedOrgNr.getTapteDagsverk() == null &&
                        sykefraværForEttKvartalMedOrgNr.getMuligeDagsverk() == null
        ));
    }

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

    @Test
    void SkalreturnereRiktigDataTilUmaskertVirksomhet() {
        List<SykefraværForEttKvartalMedOrgNr> resultat = alleVirksomheterRepository.hentSykefraværprosentAlleVirksomheterForEttKvartal(new ÅrstallOgKvartal(2019, 2));

        assertTrue(resultat.stream().anyMatch(sykefraværForEttKvartalMedOrgNr ->
                sykefraværForEttKvartalMedOrgNr.getOrgnr().equals("999999999") &&
                        !sykefraværForEttKvartalMedOrgNr.isErMaskert() &&
                        sykefraværForEttKvartalMedOrgNr.getProsent().compareTo(new BigDecimal(4)) == 0 &&
                        sykefraværForEttKvartalMedOrgNr.getTapteDagsverk().compareTo(new BigDecimal(4)) == 0 &&
                        sykefraværForEttKvartalMedOrgNr.getMuligeDagsverk().compareTo(new BigDecimal(100)) == 0
        ));

    }

    private MapSqlParameterSource parametre(int årstall, int kvartal, String orgnr, String næringskode2Siffer, String næringskode5Siffer, int antall_graderte_sykemeldinger, int tapte_dagsverk_gradert_sykemelding, int antall_sykemeldinger, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
        return new MapSqlParameterSource()
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("orgnr", orgnr)
                .addValue("naring", næringskode2Siffer)
                .addValue("naring_kode", næringskode5Siffer)
                .addValue("antall_graderte_sykemeldinger", antall_graderte_sykemeldinger)
                .addValue("tapte_dagsverk_gradert_sykemelding", tapte_dagsverk_gradert_sykemelding)
                .addValue("antall_sykemeldinger", antall_sykemeldinger)
                .addValue("antall_personer", antallPersoner)
                .addValue("tapte_dagsverk", tapteDagsverk)
                .addValue("mulige_dagsverk", muligeDagsverk);
    }
}
