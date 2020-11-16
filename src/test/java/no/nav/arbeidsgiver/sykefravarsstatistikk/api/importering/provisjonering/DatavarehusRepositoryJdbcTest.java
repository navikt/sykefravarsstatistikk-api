package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.DatavarehusRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
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

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.DatavarehusRepository.RECTYPE_FOR_FORETAK;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.autoimport.DatavarehusRepository.RECTYPE_FOR_VIRKSOMHET;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori._1_DAG_TIL_7_DAGER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori._8_DAGER_TIL_16_DAGER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("db-test")
@RunWith(SpringRunner.class)
@DataJdbcTest
public class DatavarehusRepositoryJdbcTest {

    public static final String ORGNR_VIRKSOMHET_1 = "987654321";
    public static final String ORGNR_VIRKSOMHET_2 = "999999999";
    public static final String ORGNR_VIRKSOMHET_3 = "999999999";

    public static final String NÆRINGSKODE_5SIFFER = "10062";
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private DatavarehusRepository repository;

    @Before
    public void setUp() {
        repository = new DatavarehusRepository(namedParameterJdbcTemplate);
        cleanUpTestDb(namedParameterJdbcTemplate);
    }

    @After
    public void tearDown() {
        cleanUpTestDb(namedParameterJdbcTemplate);
    }

    @Test
    public void hentSisteÅrstallOgKvartalFraSykefraværsstatistikk__returnerer_siste_ÅrstallOgKvartal_for_Land_og_sektor() {
        insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2019, 4, 4, 5, 100);
        insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2019, 4, 6, 10, 100);
        insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2020, 1, 1, 1, 10);

        ÅrstallOgKvartal sisteÅrstallOgKvartal =
                repository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(StatistikkildeDvh.LAND_OG_SEKTOR);

        assertEquals(new ÅrstallOgKvartal(2020, 1), sisteÅrstallOgKvartal);
    }

    @Test
    public void hentSisteÅrstallOgKvartalFraSykefraværsstatistikk__returnerer_siste_ÅrstallOgKvartal_for_Næring() {
        insertSykefraværsstatistikkNæringInDvhTabell(
                namedParameterJdbcTemplate,
                2019,
                4,
                4,
                "23",
                "K",
                5,
                100
        );
        insertSykefraværsstatistikkNæringInDvhTabell(
                namedParameterJdbcTemplate,
                2020,
                1,
                2,
                "90",
                "M",
                12,
                100
        );

        ÅrstallOgKvartal sisteÅrstallOgKvartal =
                repository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(StatistikkildeDvh.NÆRING);

        assertEquals(new ÅrstallOgKvartal(2020, 1), sisteÅrstallOgKvartal);
    }

    @Test
    public void hentSisteÅrstallOgKvartalFraSykefraværsstatistikk__returnerer_siste_ÅrstallOgKvartal_for_Næring5Siffer() {
        insertSykefraværsstatistikkNærin5SiffergInDvhTabell(
                namedParameterJdbcTemplate,
                2020,
                2,
                4,
                "01110",
                "K",
                5,
                100
        );
        insertSykefraværsstatistikkNærin5SiffergInDvhTabell(
                namedParameterJdbcTemplate,
                2020,
                1,
                2,
                "01110",
                "M",
                12,
                100
        );

        ÅrstallOgKvartal sisteÅrstallOgKvartal =
                repository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(StatistikkildeDvh.NÆRING_5_SIFFER);

        assertEquals(new ÅrstallOgKvartal(2020, 2), sisteÅrstallOgKvartal);
    }

    @Test
    public void hentSisteÅrstallOgKvartalFraSykefraværsstatistikk__returnerer_siste_ÅrstallOgKvartal_for_Virksomhet() {
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
                namedParameterJdbcTemplate,
                2018,
                4,
                4, ORGNR_VIRKSOMHET_1,"10062",
                _8_DAGER_TIL_16_DAGER,
                "K",
                5, 100
        );
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
                namedParameterJdbcTemplate,
                2019,
                1,
                5,
                ORGNR_VIRKSOMHET_2, "10062",
                _8_DAGER_TIL_16_DAGER,
                "M",
                5,
                101
        );

        ÅrstallOgKvartal sisteÅrstallOgKvartal =
                repository.hentSisteÅrstallOgKvartalForSykefraværsstatistikk(StatistikkildeDvh.VIRKSOMHET);

        assertEquals(new ÅrstallOgKvartal(2019, 1), sisteÅrstallOgKvartal);
    }

    @Test
    public void hentSykefraværsstatistikkSektor__lager_sum_og_returnerer_antall_tapte_og_mulige_dagsverk() {
        insertSykefraværsstatistikkLandInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 1, 5, 100);
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
        insertSykefraværsstatistikkVirksomhetInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 4, ORGNR_VIRKSOMHET_1, "10062", _1_DAG_TIL_7_DAGER, "K", 5, 100);
        insertSykefraværsstatistikkVirksomhetInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 3, ORGNR_VIRKSOMHET_1, "10062", _1_DAG_TIL_7_DAGER, "M", 8, 88);
        insertSykefraværsstatistikkVirksomhetInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 6, ORGNR_VIRKSOMHET_2, "10062", _1_DAG_TIL_7_DAGER, "K", 3, 75);
        insertSykefraværsstatistikkVirksomhetInDvhTabell(namedParameterJdbcTemplate, 2019, 1, 5, ORGNR_VIRKSOMHET_1, "10062", _1_DAG_TIL_7_DAGER, "M", 5, 101);
        insertSykefraværsstatistikkVirksomhetInDvhTabell(namedParameterJdbcTemplate, 2019, 2, 9, ORGNR_VIRKSOMHET_1, "10062", _8_DAGER_TIL_16_DAGER, "M", 9, 99);

        List<SykefraværsstatistikkVirksomhet> sykefraværsstatistikkVirksomhet =
                repository.hentSykefraværsstatistikkVirksomhet(new ÅrstallOgKvartal(2018, 4));

        assertThat(sykefraværsstatistikkVirksomhet, hasSize(2));
        SykefraværsstatistikkVirksomhet expected = new SykefraværsstatistikkVirksomhet(
                2018,
                4,
                ORGNR_VIRKSOMHET_1,
                _1_DAG_TIL_7_DAGER.kode,
                7,
                new BigDecimal(13).setScale(6),
                new BigDecimal(188).setScale(6)
        );
        assertThat(sykefraværsstatistikkVirksomhet.get(0), equalTo(expected));
    }

    @Test
    public void hentSykefraværsstatistikkNæringMedVarighet__lager_sum_og_returnerer_antall_tapte_og_mulige_dagsverk_med_varighet() {
        insertSykefraværsstatistikkVirksomhetInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 4, ORGNR_VIRKSOMHET_1, "10062", _1_DAG_TIL_7_DAGER, "K", 5, 100);
        insertSykefraværsstatistikkVirksomhetInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 4, ORGNR_VIRKSOMHET_1, "10062", _1_DAG_TIL_7_DAGER, "K", 1, 10, RECTYPE_FOR_FORETAK);
        insertSykefraværsstatistikkVirksomhetInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 4, ORGNR_VIRKSOMHET_1, "10062", _8_DAGER_TIL_16_DAGER, "K", 5, 100);
        insertSykefraværsstatistikkVirksomhetInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 3, ORGNR_VIRKSOMHET_1, "10062", _1_DAG_TIL_7_DAGER, "M", 8, 88);
        insertSykefraværsstatistikkVirksomhetInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 6, ORGNR_VIRKSOMHET_2, "10062", _1_DAG_TIL_7_DAGER, "K", 3, 75);
        insertSykefraværsstatistikkVirksomhetInDvhTabell(namedParameterJdbcTemplate, 2018, 4, 6, ORGNR_VIRKSOMHET_3, "85000", _1_DAG_TIL_7_DAGER, "K", 10, 80);

        insertSykefraværsstatistikkVirksomhetInDvhTabell(namedParameterJdbcTemplate, 2019, 1, 5, ORGNR_VIRKSOMHET_1, "10062", _1_DAG_TIL_7_DAGER, "M", 5, 101);
        insertSykefraværsstatistikkVirksomhetInDvhTabell(namedParameterJdbcTemplate, 2019, 2, 9, ORGNR_VIRKSOMHET_3, "85000", _8_DAGER_TIL_16_DAGER, "M", 9, 99);

        List<SykefraværsstatistikkNæringMedVarighet> sykefraværsstatistikkNæringMedVarighet =
                repository.hentSykefraværsstatistikkNæringMedVarighet(new ÅrstallOgKvartal(2018, 4));

        assertThat(sykefraværsstatistikkNæringMedVarighet, hasSize(3));
        SykefraværsstatistikkNæringMedVarighet expected = new SykefraværsstatistikkNæringMedVarighet(
                2018,
                4,
                NÆRINGSKODE_5SIFFER,
                _1_DAG_TIL_7_DAGER.kode,
                13,
                new BigDecimal(16).setScale(6),
                new BigDecimal(263).setScale(6)
        );
        assertThat(sykefraværsstatistikkNæringMedVarighet.get(0), equalTo(expected));
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

    private static void cleanUpTestDb(NamedParameterJdbcTemplate jdbcTemplate) {
        delete(jdbcTemplate, "dt_p.v_dim_ia_naring_sn2007");
        delete(jdbcTemplate, "dt_p.v_dim_ia_sektor");
        delete(jdbcTemplate, "dt_p.agg_ia_sykefravar_land_v");
        delete(jdbcTemplate, "dt_p.agg_ia_sykefravar_v");
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
                "insert into dt_p.agg_ia_sykefravar_land_v ("
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

    private static void insertSykefraværsstatistikkVirksomhetInDvhTabell(
            NamedParameterJdbcTemplate jdbcTemplate,
            int årstall,
            int kvartal,
            int antallPersoner,
            String orgnr,
            String næringskode5siffer,
            Varighetskategori varighet,
            String kjonn,
            long taptedagsverk,
            long muligedagsverk) {
        insertSykefraværsstatistikkVirksomhetInDvhTabell(
                jdbcTemplate,
                årstall,
                kvartal,
                antallPersoner,
                orgnr,
                næringskode5siffer,
                varighet,
                kjonn,
                taptedagsverk,
                muligedagsverk,
                RECTYPE_FOR_VIRKSOMHET
        );
    }

    private static void insertSykefraværsstatistikkVirksomhetInDvhTabell(
            NamedParameterJdbcTemplate jdbcTemplate,
            int årstall,
            int kvartal,
            int antallPersoner,
            String orgnr,
            String næringskode5siffer,
            Varighetskategori varighet,
            String kjonn,
            long taptedagsverk,
            long muligedagsverk,
            String rectype) {
        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("arstall", årstall)
                        .addValue("kvartal", kvartal)
                        .addValue("antpers", antallPersoner)
                        .addValue("orgnr", orgnr)
                        .addValue("varighet", varighet.kode)
                        .addValue("naering_kode", næringskode5siffer)
                        .addValue("kjonn", kjonn)
                        .addValue("taptedv", taptedagsverk)
                        .addValue("muligedv", muligedagsverk)
                        .addValue("rectype", rectype);

        jdbcTemplate.update(
                "insert into dt_p.agg_ia_sykefravar_v ("
                        + "arstall, kvartal, "
                        + "orgnr, naering_kode, sektor, storrelse, fylkarb, "
                        + "alder, kjonn,  fylkbo, "
                        + "sftype, varighet, "
                        + "taptedv, muligedv, antpers, rectype) "
                        + "values ("
                        + ":arstall, :kvartal, "
                        + ":orgnr, :naering_kode, '3', 'G', '03', "
                        + "'B', :kjonn, '02', "
                        + "'L', :varighet, "
                        + ":taptedv, :muligedv, :antpers, :rectype)",
                params);
    }

    private static void insertSykefraværsstatistikkNæringInDvhTabell(
            NamedParameterJdbcTemplate jdbcTemplate,
            int årstall,
            int kvartal,
            int antallPersoner,
            String næring,
            String kjonn,
            long taptedagsverk,
            long muligedagsverk) {
        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("arstall", årstall)
                        .addValue("kvartal", kvartal)
                        .addValue("antpers", antallPersoner)
                        .addValue("naring", næring)
                        .addValue("kjonn", kjonn)
                        .addValue("taptedv", taptedagsverk)
                        .addValue("muligedv", muligedagsverk);

        jdbcTemplate.update(
                "insert into dt_p.v_agg_ia_sykefravar_naring ("
                        + "arstall, kvartal, "
                        + "naring, "
                        + "alder, kjonn, "
                        + "taptedv, muligedv, antpers) "
                        + "values ("
                        + ":arstall, :kvartal, "
                        + ":naring, "
                        + "'A', :kjonn, "
                        + ":taptedv, :muligedv, :antpers)",
                params);
    }

    private static void insertSykefraværsstatistikkNærin5SiffergInDvhTabell(
            NamedParameterJdbcTemplate jdbcTemplate,
            int årstall,
            int kvartal,
            int antallPersoner,
            String næringKode,
            String kjonn,
            long taptedagsverk,
            long muligedagsverk) {
        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("arstall", årstall)
                        .addValue("kvartal", kvartal)
                        .addValue("antpers", antallPersoner)
                        .addValue("næringKode", næringKode)
                        .addValue("kjonn", kjonn)
                        .addValue("taptedv", taptedagsverk)
                        .addValue("muligedv", muligedagsverk);

        jdbcTemplate.update(
                "insert into dt_p.agg_ia_sykefravar_naring_kode ("
                        + "arstall, kvartal, "
                        + "naering_kode, "
                        + "alder, kjonn, "
                        + "taptedv, muligedv, antpers) "
                        + "values ("
                        + ":arstall, :kvartal, "
                        + ":næringKode, "
                        + "'A', :kjonn, "
                        + ":taptedv, :muligedv, :antpers)",
                params);
    }
}
