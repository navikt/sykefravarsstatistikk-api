package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.SISTE_PUBLISERTE_KVARTAL;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestUtils.sisteKvartalMinus;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.ArbeidsmiljøportalenBransje.BARNEHAGER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import java.util.Map;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.dataTilFrontends.Aggregeringskalkulator;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Sykefraværsdata;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.UmaskertSykefraværForEttKvartal;
import org.junit.jupiter.api.Test;

class AggregeringskalkulatorTest {

  @Test
  void fraværsprosentLand_regnerUtRiktigFraværsprosent() {
    Aggregeringskalkulator kalkulator =
        new Aggregeringskalkulator(
            new Sykefraværsdata(Map.of(Statistikkategori.VIRKSOMHET, synkendeSykefravær)),
            SISTE_PUBLISERTE_KVARTAL);

    assertThat(kalkulator.fraværsprosentVirksomhet("dummynavn").get().getVerdi()).isEqualTo("5.0");
  }

  @Test
  void fraværsprosentBransjeEllerNæring_regnerUtRiktigFraværsprosentForBransje() {
    Aggregeringskalkulator kalkulator =
        new Aggregeringskalkulator(
            new Sykefraværsdata(Map.of(Statistikkategori.BRANSJE, synkendeSykefravær)),
            SISTE_PUBLISERTE_KVARTAL);

    BransjeEllerNæring bransje =
        new BransjeEllerNæring(new Bransje(BARNEHAGER, "Barnehager", "88911"));

    assertThat(kalkulator.fraværsprosentBransjeEllerNæring(bransje).get().getVerdi())
        .isEqualTo("5.0");
  }

  @Test
  void fraværsprosentBransjeEllerNæring_regnerUtRiktigFraværsprosentForNæring() {
    Aggregeringskalkulator kalkulator =
        new Aggregeringskalkulator(
            new Sykefraværsdata(Map.of(Statistikkategori.NÆRING, synkendeSykefravær)),
            SISTE_PUBLISERTE_KVARTAL);

    BransjeEllerNæring dummynæring = new BransjeEllerNæring(new Næring("00000", "Dummynæring"));

    assertThat(kalkulator.fraværsprosentBransjeEllerNæring(dummynæring).get().getVerdi())
        .isEqualTo("5.0");
  }

  @Test
  void fraværsprosentNorge_regnerUtRiktigFraværsprosent() {
    Aggregeringskalkulator kalkulator =
        new Aggregeringskalkulator(
            new Sykefraværsdata(Map.of(Statistikkategori.LAND, synkendeSykefravær)),
            SISTE_PUBLISERTE_KVARTAL);

    assertThat(kalkulator.fraværsprosentNorge().get().getVerdi()).isEqualTo("5.0");
  }

  @Test
  void trendBransjeEllerNæring_regnerUtRiktigTrendForNæring() {
    Aggregeringskalkulator kalkulator =
        new Aggregeringskalkulator(
            new Sykefraværsdata(Map.of(Statistikkategori.NÆRING, synkendeSykefravær)),
            SISTE_PUBLISERTE_KVARTAL);

    BransjeEllerNæring dummynæring = new BransjeEllerNæring(new Næring("00000", "Dummynæring"));

    assertThat(kalkulator.trendBransjeEllerNæring(dummynæring).get().getVerdi()).isEqualTo("-8.0");
  }

  private final List<UmaskertSykefraværForEttKvartal> synkendeSykefravær =
      List.of(
          new UmaskertSykefraværForEttKvartal(sisteKvartalMinus(0), 2, 100, 10),
          new UmaskertSykefraværForEttKvartal(sisteKvartalMinus(1), 4, 100, 10),
          new UmaskertSykefraværForEttKvartal(sisteKvartalMinus(2), 6, 100, 10),
          new UmaskertSykefraværForEttKvartal(sisteKvartalMinus(3), 8, 100, 10),
          new UmaskertSykefraværForEttKvartal(sisteKvartalMinus(4), 10, 100, 10));
}
