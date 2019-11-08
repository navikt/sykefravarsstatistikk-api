package no.nav.tag.sykefravarsstatistikk.api.provisjonering;

import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkLand;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næringsgruppe;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("db-test")
@RunWith(SpringRunner.class)
@DataJdbcTest
public class DataverehusRepositoryJdbcTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private DataverehusRepository repository;

    @Before
    public void setUp() {
        repository = new DataverehusRepository(namedParameterJdbcTemplate);
        cleanUpTestDb(namedParameterJdbcTemplate);
    }

    @After
    public void tearDown() {
        cleanUpTestDb(namedParameterJdbcTemplate);
    }

    @Test
    public void hentSykefraværsstatistikkLand_lager_sum_og_returnerer_antall_tapte_og_mulige_dagsverk() {
        insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 5, 100);
        insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 10, 100);
        insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2019, 1, 1, 10);

        List<SykefraværsstatistikkLand> sykefraværsstatistikkLand =
                repository.hentSykefraværsstatistikkLand(new ÅrstallOgKvartal(2018, 4));

        assertThat(sykefraværsstatistikkLand, hasSize(1));
        assertEquals(
                new SykefraværsstatistikkLand(
                        2018,
                        4,
                        new BigDecimal(15).setScale(6),
                        new BigDecimal(200).setScale(6)
                ),
                sykefraværsstatistikkLand.get(0));
    }

    @Test
    public void hentSykefraværsstatistikkLand_returnerer_en_tom_liste_dersom_ingen_data_finnes_i_DVH() {
        insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2019, 1, 1, 10);

        List<SykefraværsstatistikkLand> sykefraværsstatistikkLand =
                repository.hentSykefraværsstatistikkLand(new ÅrstallOgKvartal(2018, 4));

        assertTrue(sykefraværsstatistikkLand.isEmpty());
    }

    @Test
    public void hentSektor_returnerer_eksisterende_Sektor() {
        insertSektorInDvhTabell(namedParameterJdbcTemplate, "9", "Fylkeskommunal forvaltning");
        insertSektorInDvhTabell(namedParameterJdbcTemplate, "0", "Ukjent");

        List<Sektor> sektorer = repository.hentAlleSektorer();

        assertTrue(sektorer.size() == 2);
        assertTrue(sektorer.contains(new Sektor("0", "Ukjent")));
        assertTrue(sektorer.contains(new Sektor("9", "Fylkeskommunal forvaltning")));
    }

    @Test
    public void hentNæring_returnerer_eksisterende_Næring() {
        insertNæringsgruppeInDvhTabell(namedParameterJdbcTemplate, "B", "Jordbruk, skogbruk og fiske");
        insertNæringInDvhTabell(namedParameterJdbcTemplate, "14", "B", "Jordbruk, skogbruk og fiske");
        insertNæringInDvhTabell(namedParameterJdbcTemplate, "15", "B", "Turisme");

        List<Næring> næringer = repository.hentAlleNæringer();

        assertTrue(næringer.contains(new Næring("B", "14", "Jordbruk, skogbruk og fiske")));
        assertTrue(næringer.contains(new Næring("B", "15", "Turisme")));
    }

    @Test
    public void hentNæringsgruppe_returnerer_eksisterende_Næringsgruppe() {
        insertNæringsgruppeInDvhTabell(namedParameterJdbcTemplate, "B", "Jordbruk, skogbruk og fiske");
        insertNæringsgruppeInDvhTabell(namedParameterJdbcTemplate, "C", "Industri");

        List<Næringsgruppe> næringsgrupper = repository.hentAlleNæringsgrupper();

        assertTrue(næringsgrupper.contains(new Næringsgruppe("C", "Industri")));
        assertTrue(næringsgrupper.contains(new Næringsgruppe("B", "Jordbruk, skogbruk og fiske")));
    }

    private static void cleanUpTestDb(NamedParameterJdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("DELETE FROM dt_p.v_dim_ia_naring_sn2007", new MapSqlParameterSource());
        jdbcTemplate.update(
                "DELETE FROM dt_p.v_dim_ia_fgrp_naring_sn2007", new MapSqlParameterSource());
        jdbcTemplate.update("DELETE FROM dt_p.v_dim_ia_sektor", new MapSqlParameterSource());
        jdbcTemplate.update("DELETE FROM dt_p.v_agg_ia_sykefravar_land", new MapSqlParameterSource());
    }

    private static void insertSektorInDvhTabell(
            NamedParameterJdbcTemplate jdbcTemplate, String kode, String navn) {
        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("sektorkode", kode)
                        .addValue("sektornavn", navn);

        jdbcTemplate.update(
                "INSERT INTO dt_p.v_dim_ia_sektor (sektorkode, sektornavn) "
                        + "VALUES (:sektorkode, :sektornavn)",
                params);
    }

    private static void insertNæringsgruppeInDvhTabell(
            NamedParameterJdbcTemplate jdbcTemplate, String næringsgruppekode, String næringsgruppenavn) {
        MapSqlParameterSource naringsgruppeParams =
                new MapSqlParameterSource()
                        .addValue("nargrpkode", næringsgruppekode)
                        .addValue("nargrpnavn", næringsgruppenavn);

        jdbcTemplate.update(
                "INSERT INTO dt_p.v_dim_ia_fgrp_naring_sn2007 (nargrpkode, nargrpnavn) "
                        + "VALUES (:nargrpkode, :nargrpnavn)",
                naringsgruppeParams);
    }

    private static void insertNæringInDvhTabell(
            NamedParameterJdbcTemplate jdbcTemplate,
            String næringkode,
            String næringsgruppekode,
            String næringnavn) {
        MapSqlParameterSource naringParams =
                new MapSqlParameterSource()
                        .addValue("naringkode", næringkode)
                        .addValue("nargrpkode", næringsgruppekode)
                        .addValue("naringnavn", næringnavn);

        jdbcTemplate.update(
                "INSERT INTO dt_p.v_dim_ia_naring_sn2007 (naringkode, nargrpkode, naringnavn) "
                        + "VALUES (:naringkode, :nargrpkode, :naringnavn)",
                naringParams);
    }

    private static void insertSykefraværsstatistikkLandInDvhTabell(
            NamedParameterJdbcTemplate jdbcTemplate,
            int årstall,
            int kvartal,
            long taptedagsverk,
            long muligedagsverk) {
        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("arstall", årstall)
                        .addValue("kvartal", kvartal)
                        .addValue("taptedv", taptedagsverk)
                        .addValue("muligedv", muligedagsverk);

        jdbcTemplate.update(
                "INSERT INTO dt_p.V_AGG_IA_SYKEFRAVAR_LAND ("
                        + "arstall, kvartal, "
                        + "naring, naringnavn, "
                        + "alder, kjonn, "
                        + "fylkbo, fylknavn, "
                        + "varighet, sektor, sektornavn, "
                        + "taptedv, muligedv) "
                        + "VALUES ("
                        + ":arstall, :kvartal, "
                        + "'41', 'Bygge- og anleggsvirksomhet', "
                        + "'D', 'M', "
                        + "'06', 'Buskerud', "
                        + "'B', '1', 'Statlig forvaltning', "
                        + ":taptedv, :muligedv)",
                params);
    }
}