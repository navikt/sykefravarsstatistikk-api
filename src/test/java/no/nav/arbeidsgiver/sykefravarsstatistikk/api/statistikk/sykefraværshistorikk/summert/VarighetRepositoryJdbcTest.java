package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.summert;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Orgnr;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.ArbeidsmiljøportalenBransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartalMedVarighet;
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

@ActiveProfiles("db-test")
@DataJdbcTest
public class VarighetRepositoryJdbcTest {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    private VarighetRepository varighetRepository;

    @BeforeEach
    public void setUp() {
        varighetRepository = new VarighetRepository(jdbcTemplate);
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @AfterEach
    public void tearDown() {
        slettAllStatistikkFraDatabase(jdbcTemplate);
    }

    @Test
    public void hentSykefraværForEttKvartalMedVarighet__skal_returnere_riktig_sykefravær() {
        Underenhet barnehage = Underenhet.builder().orgnr(new Orgnr("999999999"))
                .navn("test Barnehage")
                .næringskode(new Næringskode5Siffer("88911", "Barnehage"))
                .antallAnsatte(10)
                .overordnetEnhetOrgnr(new Orgnr("1111111111")).build();
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_virksomhet (arstall, kvartal, orgnr, varighet, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :orgnr, :varighet, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(2019, 2, barnehage.getOrgnr().getVerdi(), "A", 0, 4, 0)
        );
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_virksomhet (arstall, kvartal, orgnr, varighet, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (:arstall, :kvartal, :orgnr, :varighet, :antall_personer, :tapte_dagsverk, :mulige_dagsverk)",
                parametre(2019, 2, barnehage.getOrgnr().getVerdi(), "X", 6, 0, 100)
        );

        List<UmaskertSykefraværForEttKvartalMedVarighet> resultat = varighetRepository.hentSykefraværForEttKvartalMedVarighet(barnehage);

        assertThat(resultat.size()).isEqualTo(2);
        assertThat(resultat.get(0)).isEqualTo(new UmaskertSykefraværForEttKvartalMedVarighet(
                new ÅrstallOgKvartal(2019, 2),
                new BigDecimal(4),
                new BigDecimal(0),
                0,
                Varighetskategori._1_DAG_TIL_7_DAGER
        ));

        assertThat(resultat.get(1)).isEqualTo(new UmaskertSykefraværForEttKvartalMedVarighet(
                new ÅrstallOgKvartal(2019, 2),
                new BigDecimal(0),
                new BigDecimal(100),
                6,
                Varighetskategori.TOTAL
        ));
    }

    @Test
    public void hentSykefraværForEttKvartalMedVarighet_for_næring__skal_returnere_riktig_sykefravær() {
        Næringskode5Siffer barnehager = new Næringskode5Siffer("88911", "Barnehager");

        leggTilStatisitkkNæringMedVarighetForTotalVarighetskategori(barnehager,2019, 2, 1, 10);
        leggTilStatisitkkNæringMedVarighetForVarighetskategori(barnehager, 2019, 2, Varighetskategori._1_DAG_TIL_7_DAGER, 4);

        List<UmaskertSykefraværForEttKvartalMedVarighet> resultat =
                varighetRepository.hentSykefraværForEttKvartalMedVarighet(new Næring(barnehager.getKode(), ""));

        assertThat(resultat.size()).isEqualTo(2);
        assertThat(resultat.get(0)).isEqualTo(new UmaskertSykefraværForEttKvartalMedVarighet(
                new ÅrstallOgKvartal(2019, 2),
                new BigDecimal(4),
                new BigDecimal(0),
                0,
                Varighetskategori._1_DAG_TIL_7_DAGER
        ));

        assertThat(resultat.get(1)).isEqualTo(new UmaskertSykefraværForEttKvartalMedVarighet(
                new ÅrstallOgKvartal(2019, 2),
                new BigDecimal(0),
                new BigDecimal(10),
                1,
                Varighetskategori.TOTAL
        ));
    }

    @Test
    public void hentSykefraværForEttKvartalMedVarighet_for_bransje__skal_returnere_riktig_sykefravær() {
        Næringskode5Siffer sykehus = new Næringskode5Siffer("86101", "Alminnelige somatiske sykehus");
        Næringskode5Siffer legetjeneste = new Næringskode5Siffer("86211", "Allmenn legetjeneste");

        leggTilStatisitkkNæringMedVarighetForTotalVarighetskategori(sykehus,2019, 2, 1, 10);
        leggTilStatisitkkNæringMedVarighetForVarighetskategori(sykehus, 2019, 2, Varighetskategori._1_DAG_TIL_7_DAGER, 4);
        leggTilStatisitkkNæringMedVarighetForTotalVarighetskategori(legetjeneste,2019, 2, 5, 50);
        leggTilStatisitkkNæringMedVarighetForVarighetskategori(legetjeneste, 2019, 2, Varighetskategori._1_DAG_TIL_7_DAGER, 8);

        List<UmaskertSykefraværForEttKvartalMedVarighet> resultat = varighetRepository.hentSykefraværForEttKvartalMedVarighet(new Bransje(
                ArbeidsmiljøportalenBransje.SYKEHUS,
                "Sykehus",
                "86101", "86102", "86104", "86105", "86106", "86107"
        ));

        assertThat(resultat.size()).isEqualTo(2);
        assertThat(resultat.get(0)).isEqualTo(new UmaskertSykefraværForEttKvartalMedVarighet(
                new ÅrstallOgKvartal(2019, 2),
                new BigDecimal(4),
                new BigDecimal(0),
                0,
                Varighetskategori._1_DAG_TIL_7_DAGER
        ));

        assertThat(resultat.get(1)).isEqualTo(new UmaskertSykefraværForEttKvartalMedVarighet(
                new ÅrstallOgKvartal(2019, 2),
                new BigDecimal(0),
                new BigDecimal(10),
                1,
                Varighetskategori.TOTAL
        ));
    }


    private void leggTilStatisitkkNæringMedVarighetForTotalVarighetskategori(
            Næringskode5Siffer næringskode5Siffer,
            int årstall,
            int kvartal,
            int antallPersoner,
            int muligeDagsverk
            ){
        leggTilStatisitkkNæringMedVarighet(
                næringskode5Siffer,
                årstall,
                kvartal,
                Varighetskategori.TOTAL.kode,
                antallPersoner,
                0,
                muligeDagsverk
        );
    }

    private void leggTilStatisitkkNæringMedVarighetForVarighetskategori(
            Næringskode5Siffer næringskode5Siffer,
            int årstall,
            int kvartal,
            Varighetskategori varighetskategori,
            int tapteDagsverk
    ){
        leggTilStatisitkkNæringMedVarighet(
                næringskode5Siffer,
                årstall,
                kvartal,
                varighetskategori.kode,
                0,
                tapteDagsverk,
                0
        );
    };

    private void leggTilStatisitkkNæringMedVarighet(
            Næringskode5Siffer næringskode5Siffer,
            int årstall,
            int kvartal,
            String varighet,
            int antallPersoner,
            int tapteDagsverk,
            int muligeDagsverk
    ) {
        jdbcTemplate.update(
                "insert into sykefravar_statistikk_naring_med_varighet " +
                        "(arstall, kvartal, naring_kode, varighet, antall_personer, tapte_dagsverk, mulige_dagsverk) "
                        + "VALUES (" +
                        ":arstall, " +
                        ":kvartal, " +
                        ":naring_kode, " +
                        ":varighet, " +
                        ":antall_personer, " +
                        ":tapte_dagsverk, " +
                        ":mulige_dagsverk)",
                new MapSqlParameterSource()
                        .addValue("arstall", årstall)
                        .addValue("kvartal", kvartal)
                        .addValue("naring_kode", næringskode5Siffer.getKode())
                        .addValue("varighet", varighet)
                        .addValue("antall_personer", antallPersoner)
                        .addValue("tapte_dagsverk", tapteDagsverk)
                        .addValue("mulige_dagsverk", muligeDagsverk)        );
    }

    private MapSqlParameterSource parametre(int årstall, int kvartal, String orgnr, String varighet, int antallPersoner, int tapteDagsverk, int muligeDagsverk) {
        return new MapSqlParameterSource()
                .addValue("arstall", årstall)
                .addValue("kvartal", kvartal)
                .addValue("orgnr", orgnr)
                .addValue("varighet", varighet)
                .addValue("antall_personer", antallPersoner)
                .addValue("tapte_dagsverk", tapteDagsverk)
                .addValue("mulige_dagsverk", muligeDagsverk);
    }
}
