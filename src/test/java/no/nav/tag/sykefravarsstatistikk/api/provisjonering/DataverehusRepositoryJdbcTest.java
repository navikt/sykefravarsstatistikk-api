package no.nav.tag.sykefravarsstatistikk.api.provisjonering;

import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkLand;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkSektor;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.SykefraværsstatistikkVirksomhet;
import no.nav.tag.sykefravarsstatistikk.api.domene.statistikk.ÅrstallOgKvartal;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næring;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næringsgruppering;
import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Sektor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

import static no.nav.tag.sykefravarsstatistikk.api.provisjonering.DataverehusRepository.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("db-test")
@RunWith(SpringRunner.class)
@DataJdbcTest
public class DataverehusRepositoryJdbcTest {

    public static final String ORGNR_VIRKSOMHET_1 = "987654321";
    public static final String ORGNR_VIRKSOMHET_2 = "999999999";
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
    public void hentSykefraværsstatistikkSektor__lager_sum_og_returnerer_antall_tapte_og_mulige_dagsverk() {
        insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 1,5, 100);
        insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 3, 10, 100);
        insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2019, 1, 1, 1, 10);

        List<SykefraværsstatistikkSektor> sykefraværsstatistikkSektor =
                repository.hentSykefraværsstatistikkSektor(new ÅrstallOgKvartal(2018, 4));

        assertTrue(sykefraværsstatistikkSektor.size() == 1);
        SykefraværsstatistikkSektor sykefraværsstatistikkSektorExpected = new SykefraværsstatistikkSektor(2018, 4, "1", 4, new BigDecimal(15), new BigDecimal(200));
        SykefraværsstatistikkSektor sykefraværsstatistikkSektorActual = sykefraværsstatistikkSektor.get(0);
        assertTrue(new ReflectionEquals(sykefraværsstatistikkSektorExpected).matches(sykefraværsstatistikkSektorActual));
    }

    @Test
    public void hentSykefraværsstatistikkLand__lager_sum_og_returnerer_antall_tapte_og_mulige_dagsverk() {
        insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 4, 5, 100);
        insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 6, 10, 100);
        insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2019, 1, 1, 1, 10);

        List<SykefraværsstatistikkLand> sykefraværsstatistikkLand =
                repository.hentSykefraværsstatistikkLand(new ÅrstallOgKvartal(2018, 4));

        assertThat(sykefraværsstatistikkLand, hasSize(1));
        assertEquals(
                new SykefraværsstatistikkLand(
                        2018,
                        4,
                        10,
                        new BigDecimal(15).setScale(6),
                        new BigDecimal(200).setScale(6)
                ),
                sykefraværsstatistikkLand.get(0));
    }

    @Test
    public void hentSykefraværsstatistikkVirksomhet__lager_sum_og_returnerer_antall_tapte_og_mulige_dagsverk() {
        insertSykefraværsstatistikkInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 4, ORGNR_VIRKSOMHET_1, "K", 5, 100);
        insertSykefraværsstatistikkInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 3, ORGNR_VIRKSOMHET_1, "M", 8, 88);
        insertSykefraværsstatistikkInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 6, ORGNR_VIRKSOMHET_2, "K", 3, 75);
        insertSykefraværsstatistikkInDvhTabell(namedParameterJdbcTemplate, 2019, 1, 5, ORGNR_VIRKSOMHET_1, "M", 5, 101);

        List<SykefraværsstatistikkVirksomhet> sykefraværsstatistikkVirksomhet =
                repository.hentSykefraværsstatistikkVirksomhet(new ÅrstallOgKvartal(2018, 4));

        assertThat(sykefraværsstatistikkVirksomhet, hasSize(2));
        SykefraværsstatistikkVirksomhet expected = new SykefraværsstatistikkVirksomhet(
                2018,
                4,
                ORGNR_VIRKSOMHET_1,
                7,
                new BigDecimal(13).setScale(6),
                new BigDecimal(188).setScale(6)
        );
        assertThat(sykefraværsstatistikkVirksomhet.get(0), equalTo(expected));
    }

    @Test
    public void hentSykefraværsstatistikkLand__returnerer_en_tom_liste_dersom_ingen_data_finnes_i_DVH() {
        insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2019, 1, 1, 1, 10);

        List<SykefraværsstatistikkLand> sykefraværsstatistikkLand =
                repository.hentSykefraværsstatistikkLand(new ÅrstallOgKvartal(2018, 4));

        assertTrue(sykefraværsstatistikkLand.isEmpty());
    }

    @Test
    public void hentAlleSektorer__returnerer_eksisterende_Sektor() {
        insertSektorInDvhTabell(namedParameterJdbcTemplate, "9", "Fylkeskommunal forvaltning");
        insertSektorInDvhTabell(namedParameterJdbcTemplate, "0", "Ukjent");

        List<Sektor> sektorer = repository.hentAlleSektorer();

        assertTrue(sektorer.size() == 2);
        assertTrue(sektorer.contains(new Sektor("0", "Ukjent")));
        assertTrue(sektorer.contains(new Sektor("9", "Fylkeskommunal forvaltning")));
    }

    @Test
    public void hentAlleNæringer__returnerer_eksisterende_Næring() {
        insertNæringInDvhTabell(namedParameterJdbcTemplate, "02", "01", "Skogbruk og tjenester tilknyttet skogbruk");
        insertNæringInDvhTabell(namedParameterJdbcTemplate, "11", "10", "Produksjon av drikkevarer");

        List<Næring> næringer = repository.hentAlleNæringer();

        assertTrue(næringer.contains(new Næring("02", "Skogbruk og tjenester tilknyttet skogbruk")));
        assertTrue(næringer.contains(new Næring("11", "Produksjon av drikkevarer")));
    }
    
    @Test
    public void hentAlleNæringsgrupperinger__returnerer_eksisterende_Næringsgrupperinger() {
        insertNæringsgrupperingInDvhTabell(namedParameterJdbcTemplate, "02123", "Test5", "0212", "test4", "021", "test3", "02", "test2", "02", "test1");
        insertNæringsgrupperingInDvhTabell(namedParameterJdbcTemplate, "54321", "hei1", "5432", "hei2", "543", "hei3", "54", "hei4", "66633", "hei5");

        List<Næringsgruppering> næringsgrupperinger = repository.hentAlleNæringsgrupperinger();

        assertTrue(næringsgrupperinger.contains(new Næringsgruppering("02123", "Test5", "0212", "test4", "021", "test3", "02", "test2", "02", "test1")));
        assertTrue(næringsgrupperinger.contains(new Næringsgruppering("54321", "hei1", "5432", "hei2", "543", "hei3", "54", "hei4", "66633", "hei5")));
    }


    private static void cleanUpTestDb(NamedParameterJdbcTemplate jdbcTemplate) {
        delete(jdbcTemplate, "dt_p.v_dim_ia_naring_sn2007");
        delete(jdbcTemplate, "dt_p.v_dim_ia_sektor");
        delete(jdbcTemplate, "dt_p.v_agg_ia_sykefravar_land");
        delete(jdbcTemplate, "dt_p.v_agg_ia_sykefravar");
        delete(jdbcTemplate, "dt_p.dim_ia_naring");
    }

    private static int delete(NamedParameterJdbcTemplate jdbcTemplate, String tabell) {
        return jdbcTemplate.update(String.format("delete from %s", tabell), new MapSqlParameterSource());
    }

    private static void insertSektorInDvhTabell(
            NamedParameterJdbcTemplate jdbcTemplate, String kode, String navn) {
        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("sektorkode", kode)
                        .addValue("sektornavn", navn);

        jdbcTemplate.update(
                "insert into dt_p.v_dim_ia_sektor (sektorkode, sektornavn) "
                        + "values (:sektorkode, :sektornavn)",
                params);
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
                "insert into dt_p.v_dim_ia_naring_sn2007 (naringkode, nargrpkode, naringnavn) "
                        + "values (:naringkode, :nargrpkode, :naringnavn)",
                naringParams);
    }
    
    private static void insertNæringsgrupperingInDvhTabell(
            NamedParameterJdbcTemplate jdbcTemplate,
            String næringkode,
            String næringbeskrivelse,
            String gruppe1kode,
            String gruppe1beskrivelse,
            String gruppe2kode,
            String gruppe2beskrivelse,
            String gruppe3kode,
            String gruppe3beskrivelse,
            String gruppe4kode,
            String gruppe4beskrivelse
    ) {
        MapSqlParameterSource naringParams =
                new MapSqlParameterSource()
                        .addValue(NAERING_KODE, næringkode)
                        .addValue(NAERING_BESKRIVELSE, næringbeskrivelse)
                        .addValue(GRUPPE1_KODE, gruppe1kode)
                        .addValue(GRUPPE1_BESKRIVELSE, gruppe1beskrivelse)
                        .addValue(GRUPPE2_KODE, gruppe2kode)
                        .addValue(GRUPPE2_BESKRIVELSE, gruppe2beskrivelse)
                        .addValue(GRUPPE3_KODE, gruppe3kode)
                        .addValue(GRUPPE3_BESKRIVELSE, gruppe3beskrivelse)
                        .addValue(GRUPPE4_KODE, gruppe4kode)
                        .addValue(GRUPPE4_BESKRIVELSE, gruppe4beskrivelse);

        jdbcTemplate.update(
                "insert into dt_p.dim_ia_naring (naering_kode, naering_besk_lang, gruppe1_kode, gruppe1_besk_lang, gruppe2_kode, gruppe2_besk_lang, gruppe3_kode, gruppe3_besk_lang, gruppe4_kode, gruppe4_besk_lang) "
                        + "values (:naering_kode,  :naering_besk_lang,  :gruppe1_kode,  :gruppe1_besk_lang,  :gruppe2_kode,  :gruppe2_besk_lang,  :gruppe3_kode,  :gruppe3_besk_lang,  :gruppe4_kode,  :gruppe4_besk_lang)",
                naringParams);
    }

    private static void insertSykefraværsstatistikkLandInDvhTabell(
            NamedParameterJdbcTemplate jdbcTemplate,
            int årstall,
            int kvartal,
            int antallPersoner,
            long taptedagsverk,
            long muligedagsverk) {
        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("arstall", årstall)
                        .addValue("kvartal", kvartal)
                        .addValue("antpers", antallPersoner)
                        .addValue("taptedv", taptedagsverk)
                        .addValue("muligedv", muligedagsverk);

        jdbcTemplate.update(
                "insert into dt_p.V_AGG_IA_SYKEFRAVAR_LAND ("
                        + "arstall, kvartal, "
                        + "naring, naringnavn, "
                        + "alder, kjonn, "
                        + "fylkbo, fylknavn, "
                        + "varighet, sektor, sektornavn, "
                        + "taptedv, muligedv, antpers) "
                        + "values ("
                        + ":arstall, :kvartal, "
                        + "'41', 'Bygge- og anleggsvirksomhet', "
                        + "'D', 'M', "
                        + "'06', 'Buskerud', "
                        + "'B', '1', 'Statlig forvaltning', "
                        + ":taptedv, :muligedv, :antpers)",
                params);
    }

    private static void insertSykefraværsstatistikkInDvhTabell(
            NamedParameterJdbcTemplate jdbcTemplate,
            int årstall,
            int kvartal,
            int antallPersoner,
            String orgnr,
            String kjonn,
            long taptedagsverk,
            long muligedagsverk) {
        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("arstall", årstall)
                        .addValue("kvartal", kvartal)
                        .addValue("antpers", antallPersoner)
                        .addValue("orgnr", orgnr)
                        .addValue("kjonn", kjonn)
                        .addValue("taptedv", taptedagsverk)
                        .addValue("muligedv", muligedagsverk);

        jdbcTemplate.update(
                "insert into dt_p.v_agg_ia_sykefravar ("
                        + "arstall, kvartal, "
                        + "orgnr, naring, sektor, storrelse, fylkarb, "
                        + "alder, kjonn,  fylkbo, "
                        + "sftype, varighet, "
                        + "taptedv, muligedv, antpers) "
                        + "values ("
                        + ":arstall, :kvartal, "
                        + ":orgnr, '62', '3', 'G', '03', "
                        + "'B', :kjonn, '02', "
                        + "'L', 'A', "
                        + ":taptedv, :muligedv, :antpers)",
                params);
    }

}