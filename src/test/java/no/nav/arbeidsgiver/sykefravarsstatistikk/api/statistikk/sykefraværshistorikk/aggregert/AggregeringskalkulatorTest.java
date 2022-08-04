package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.ArbeidsmiljøportalenBransje.BARNEHAGER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.sisteKvartalMinus;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.testDataFactories.UmaskertSykefraværForEttKvartalTestDataFactory.opprettTestdata;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import java.util.Map;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AggregeringskalkulatorTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void fraværsprosentLand() {
        Aggregeringskalkulator kalkulator = new Aggregeringskalkulator(
              new Sykefraværsdata(Map.of(Statistikkategori.VIRKSOMHET, dummySykefravær))
        );

        assertThat(kalkulator.fraværsprosentVirksomhet("dummynavn").get().getVerdi())
              .isEqualTo("5.00");
    }

    @Test
    void fraværsprosentBransjeEllerNæring_regnerUtRiktigFraværsprosentForBransje() {
        Aggregeringskalkulator kalkulator = new Aggregeringskalkulator(
              new Sykefraværsdata(Map.of(Statistikkategori.BRANSJE, dummySykefravær))
        );

        BransjeEllerNæring bransje = new BransjeEllerNæring(new Bransje(BARNEHAGER, "Barnehager", "88911"));

        assertThat(kalkulator.fraværsprosentBransjeEllerNæring(bransje).get().getVerdi())
              .isEqualTo("5.00");
    }

    @Test
    void fraværsprosentBransjeEllerNæring_regnerUtRiktigFraværsprosentForNæring() {
        Aggregeringskalkulator kalkulator = new Aggregeringskalkulator(
              new Sykefraværsdata(Map.of(Statistikkategori.NÆRING, dummySykefravær))
        );

        BransjeEllerNæring næring = new BransjeEllerNæring(new Bransje(BARNEHAGER, "Barnehager", "88911"));

        assertThat(kalkulator.fraværsprosentBransjeEllerNæring(næring).get().getVerdi())
              .isEqualTo("5.00");
    }

    @Test
    void fraværsprosentNorge_regnerUtRiktigFraværsprosent() {
        Aggregeringskalkulator kalkulator = new Aggregeringskalkulator(
              new Sykefraværsdata(Map.of(Statistikkategori.LAND, dummySykefravær))
        );

        assertThat(kalkulator.fraværsprosentNorge().get().getVerdi())
              .isEqualTo("5.00");
    }

    @Test
    void trendBransjeEllerNæring_regnerUtRiktigTrendForNæring() {
        Aggregeringskalkulator kalkulator = new Aggregeringskalkulator(
              new Sykefraværsdata(Map.of(Statistikkategori.NÆRING, dummySykefravær))
        );

        BransjeEllerNæring næring = new BransjeEllerNæring(new Bransje(BARNEHAGER, "Barnehager", "88911"));

        assertThat(kalkulator.trendBransjeEllerNæring(næring).get().getVerdi())
              .isEqualTo("5.00");
    }

    private final List<UmaskertSykefraværForEttKvartal> dummySykefravær = List.of(
          opprettTestdata(sisteKvartalMinus(0), 2, 100, 1),
          opprettTestdata(sisteKvartalMinus(1), 4, 100, 2),
          opprettTestdata(sisteKvartalMinus(2), 6, 100, 3),
          opprettTestdata(sisteKvartalMinus(3), 8, 100, 4),
          opprettTestdata(sisteKvartalMinus(4), 10, 100, 5)
    );
}