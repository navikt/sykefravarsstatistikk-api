package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.ArbeidsmiljøportalenBransje.BARNEHAGER;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal.sisteKvartalMinus;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import java.util.Map;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransje;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.BransjeEllerNæring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.UmaskertSykefraværForEttKvartal;
import org.junit.jupiter.api.Test;

class AggregeringskalkulatorTest {

    @Test
    void fraværsprosentLand_regnerUtRiktigFraværsprosent() {
        Aggregeringskalkulator kalkulator = new Aggregeringskalkulator(
                new Sykefraværsdata(Map.of(Statistikkategori.VIRKSOMHET, synkendeSykefravær))
        );

        assertThat(kalkulator.fraværsprosentVirksomhet("dummynavn").get().getVerdi())
                .isEqualTo("5.0");
    }


    @Test
    void fraværsprosentBransjeEllerNæring_regnerUtRiktigFraværsprosentForBransje() {
        Aggregeringskalkulator kalkulator = new Aggregeringskalkulator(
                new Sykefraværsdata(Map.of(Statistikkategori.BRANSJE, synkendeSykefravær))
        );

        BransjeEllerNæring bransje = new BransjeEllerNæring(
                new Bransje(BARNEHAGER, "Barnehager", "88911"));

        assertThat(kalkulator.fraværsprosentBransjeEllerNæring(bransje).get().getVerdi())
                .isEqualTo("5.0");
    }


    @Test
    void fraværsprosentBransjeEllerNæring_regnerUtRiktigFraværsprosentForNæring() {
        Aggregeringskalkulator kalkulator = new Aggregeringskalkulator(
                new Sykefraværsdata(Map.of(Statistikkategori.NÆRING, synkendeSykefravær))
        );

        BransjeEllerNæring dummynæring = new BransjeEllerNæring(new Næring("00000", "Dummynæring"));

        assertThat(kalkulator.fraværsprosentBransjeEllerNæring(dummynæring).get().getVerdi())
                .isEqualTo("5.0");
    }


    @Test
    void fraværsprosentNorge_regnerUtRiktigFraværsprosent() {
        Aggregeringskalkulator kalkulator = new Aggregeringskalkulator(
                new Sykefraværsdata(Map.of(Statistikkategori.LAND, synkendeSykefravær))
        );

        assertThat(kalkulator.fraværsprosentNorge().get().getVerdi())
                .isEqualTo("5.0");
    }


    @Test
    void trendBransjeEllerNæring_regnerUtRiktigTrendForNæring() {
        Aggregeringskalkulator kalkulator = new Aggregeringskalkulator(
                new Sykefraværsdata(Map.of(Statistikkategori.NÆRING, synkendeSykefravær))
        );

        BransjeEllerNæring dummynæring
                = new BransjeEllerNæring(new Næring("00000", "Dummynæring"));

        assertThat(kalkulator.trendBransjeEllerNæring(dummynæring).get().getVerdi())
                .isEqualTo("-8.0");
    }


    private final List<UmaskertSykefraværForEttKvartal> synkendeSykefravær = List.of(
            new UmaskertSykefraværForEttKvartal(sisteKvartalMinus(0), 2, 100, 1),
            new UmaskertSykefraværForEttKvartal(sisteKvartalMinus(1), 4, 100, 2),
            new UmaskertSykefraværForEttKvartal(sisteKvartalMinus(2), 6, 100, 3),
            new UmaskertSykefraværForEttKvartal(sisteKvartalMinus(3), 8, 100, 4),
            new UmaskertSykefraværForEttKvartal(sisteKvartalMinus(4), 10, 100, 5)
    );
}
