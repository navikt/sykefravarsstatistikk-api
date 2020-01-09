package no.nav.tag.sykefravarsstatistikk.api.virksomhetsklassifikasjoner;

import no.nav.tag.sykefravarsstatistikk.api.domene.virksomhetsklassifikasjoner.Næringsgruppering;
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

import static no.nav.tag.sykefravarsstatistikk.api.TestUtils.enNæringsgruppering;
import static no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.NæringsgrupperingSynkroniseringRepository.*;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("db-test")
@RunWith(SpringRunner.class)
@DataJdbcTest
public class KlassifikasjonerRepositoryJdbcTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private KlassifikasjonerRepository repository;

    @Before
    public void setUp() {
        repository = new KlassifikasjonerRepository(namedParameterJdbcTemplate);
        cleanUpTestDb(namedParameterJdbcTemplate);
    }

    @After
    public void tearDown() {
        cleanUpTestDb(namedParameterJdbcTemplate);
    }


    @Test
    public void hentSektor_returnerer_eksisterende_Sektor() {
        insertSektor(namedParameterJdbcTemplate, "9", "Fylkeskommunal forvaltning");

        Sektor sektor = repository.hentSektor("9");

        assertThat(sektor.getKode()).isEqualTo("9");
        assertThat(sektor.getNavn()).isEqualTo("Fylkeskommunal forvaltning");
    }

    @Test
    public void hentNæringsgruppering__returnerer_eksisterende_Næringsgruppering() {
        Næringsgruppering næringsgruppering = enNæringsgruppering();
        insertNæringsgruppering(namedParameterJdbcTemplate, næringsgruppering);

        Næringsgruppering hentetNæringsgruppering = repository.hentNæringsgruppering(næringsgruppering.getKode5siffer());

        assertThat(hentetNæringsgruppering).isEqualTo(næringsgruppering);
    }

    @Test
    public void hentNæringsgrupperinger__skal_returnere_Næringsgrupperinger_som_starter_med_angitte_sifre() {
        Næringsgruppering næringsgruppering1 = enNæringsgruppering("11111");
        Næringsgruppering næringsgruppering2 = enNæringsgruppering("11222");
        Næringsgruppering næringsgruppering3 = enNæringsgruppering("33333");
        insertNæringsgruppering(namedParameterJdbcTemplate, næringsgruppering1);
        insertNæringsgruppering(namedParameterJdbcTemplate, næringsgruppering2);
        insertNæringsgruppering(namedParameterJdbcTemplate, næringsgruppering3);

        List<Næringsgruppering> resultat = repository.hentNæringsgrupperingerTilhørendeNæringskode2siffer("11");

        assertThat(resultat).containsExactly(næringsgruppering1, næringsgruppering2);
    }

    @Test
    public void hentNæringsgrupperinger__skal_returnere_tom_liste_hvis_ingen_næringsgrupperinger_eksisterer() {
        assertThat(repository.hentNæringsgrupperingerTilhørendeNæringskode2siffer("11")).isEmpty();
    }

    private static void cleanUpTestDb(NamedParameterJdbcTemplate jdbcTemplate) {
        jdbcTemplate.update("delete from sektor", new MapSqlParameterSource());
    }

    private static void insertNæringsgruppering(NamedParameterJdbcTemplate jdbcTemplate, Næringsgruppering næringsgruppering) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(KODE_5SIFFER, næringsgruppering.getKode5siffer())
                .addValue(BESKRIVELSE_5SIFFER, næringsgruppering.getBeskrivelse5siffer())
                .addValue(KODE_4SIFFER, næringsgruppering.getKode4siffer())
                .addValue(BESKRIVELSE_4SIFFER, næringsgruppering.getBeskrivelse4siffer())
                .addValue(KODE_3SIFFER, næringsgruppering.getKode3siffer())
                .addValue(BESKRIVELSE_3SIFFER, næringsgruppering.getBeskrivelse3siffer())
                .addValue(KODE_2SIFFER, næringsgruppering.getKode2siffer())
                .addValue(BESKRIVELSE_2SIFFER, næringsgruppering.getBeskrivelse2siffer())
                .addValue(KODE_HOVEDOMRADE, næringsgruppering.getKodeHovedområde())
                .addValue(BESKRIVELSE_HOVEDOMRADE, næringsgruppering.getBeskrivelseHovedområde());

        jdbcTemplate.update(
                "insert into naringsgruppering (kode_5siffer, beskrivelse_5siffer, kode_4siffer, beskrivelse_4siffer, kode_3siffer, beskrivelse_3siffer, kode_2siffer, beskrivelse_2siffer, kode_hovedomrade, beskrivelse_hovedomrade)  " +
                        "values (:kode_5siffer, :beskrivelse_5siffer, :kode_4siffer, :beskrivelse_4siffer, :kode_3siffer, :beskrivelse_3siffer, :kode_2siffer, :beskrivelse_2siffer, :kode_hovedomrade, :beskrivelse_hovedomrade)",
                params
        );
    }

    private static void insertSektor(NamedParameterJdbcTemplate jdbcTemplate, String kode, String navn) {
        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("kode", kode)
                        .addValue("navn", navn);

        jdbcTemplate.update("insert into sektor (kode, navn) values (:kode, :navn)", params);
    }
}
