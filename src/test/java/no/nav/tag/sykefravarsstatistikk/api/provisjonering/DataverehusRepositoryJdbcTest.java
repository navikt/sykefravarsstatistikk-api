package no.nav.tag.sykefravarsstatistikk.api.provisjonering;

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

import java.util.List;

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
        insertNæringsgruppeInDvhTabell(namedParameterJdbcTemplate,
                "B",
                "Jordbruk, skogbruk og fiske"
        );
        insertNæringInDvhTabell(namedParameterJdbcTemplate,
                "14",
                "B",
                "Jordbruk, skogbruk og fiske"
        );
        insertNæringInDvhTabell(namedParameterJdbcTemplate,
                "15",
                "B",
                "Turisme"
        );

        List<Næring> næringer = repository.hentAlleNæringer();

        assertTrue(næringer.contains(new Næring("B", "14", "Jordbruk, skogbruk og fiske")));
        assertTrue(næringer.contains(new Næring("B", "15", "Turisme")));
    }

    @Test
    public void hentNæringsgruppe_returnerer_eksisterende_Næringsgruppe() {
        insertNæringsgruppeInDvhTabell(namedParameterJdbcTemplate,
                "B",
                "Jordbruk, skogbruk og fiske"
        );
        insertNæringsgruppeInDvhTabell(namedParameterJdbcTemplate,
                "C",
                "Industri"
        );


        List<Næringsgruppe> næringsgrupper = repository.hentAlleNæringsgrupper();

        assertTrue(næringsgrupper.contains(new Næringsgruppe("C", "Industri")));
        assertTrue(næringsgrupper.contains(new Næringsgruppe("B", "Jordbruk, skogbruk og fiske")));
    }


    private static void cleanUpTestDb(NamedParameterJdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("delete from dt_p.V_DIM_IA_NARING_SN2007", new MapSqlParameterSource());
        jdbcTemplate.update("delete from dt_p.V_DIM_IA_FGRP_NARING_SN2007", new MapSqlParameterSource());
        jdbcTemplate.update("delete from dt_p.V_DIM_IA_SEKTOR", new MapSqlParameterSource());
    }

    private static void insertSektorInDvhTabell(NamedParameterJdbcTemplate jdbcTemplate, String kode, String navn) {
        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("sektorkode", kode)
                        .addValue("sektornavn", navn);

        jdbcTemplate.update("insert into dt_p.V_DIM_IA_SEKTOR (SEKTORKODE, SEKTORNAVN) " +
                "values (:sektorkode, :sektornavn)", params);
    }

    private static void insertNæringsgruppeInDvhTabell(
            NamedParameterJdbcTemplate jdbcTemplate,
            String næringsgruppekode,
            String næringsgruppenavn
    ) {
        MapSqlParameterSource naringsgruppeParams =
                new MapSqlParameterSource()
                        .addValue("NARGRPKODE", næringsgruppekode)
                        .addValue("NARGRPNAVN", næringsgruppenavn);

        jdbcTemplate.update("insert into dt_p.V_DIM_IA_FGRP_NARING_SN2007 (NARGRPKODE, NARGRPNAVN) " +
                "values (:NARGRPKODE, :NARGRPNAVN)", naringsgruppeParams);
    }

    private static void insertNæringInDvhTabell(
            NamedParameterJdbcTemplate jdbcTemplate,
            String næringkode,
            String næringsgruppekode,
            String næringnavn
    ) {
        MapSqlParameterSource naringParams =
                new MapSqlParameterSource()
                        .addValue("NARINGKODE", næringkode)
                        .addValue("NARGRPKODE", næringsgruppekode)
                        .addValue("NARINGNAVN", næringnavn);

        jdbcTemplate.update("insert into dt_p.V_DIM_IA_NARING_SN2007 (NARINGKODE, NARGRPKODE, NARINGNAVN) " +
                "values (:NARINGKODE, :NARGRPKODE, :NARINGNAVN)", naringParams);
    }
}