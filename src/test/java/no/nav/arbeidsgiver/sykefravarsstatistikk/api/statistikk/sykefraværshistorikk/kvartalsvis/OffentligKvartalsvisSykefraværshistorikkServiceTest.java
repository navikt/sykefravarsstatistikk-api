package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering.KvartalsvisSykefraværshistorikkService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering.OffentligKvartalsvisSykefraværshistorikkService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.KvartalsvisSykefraværRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.enNæringskode;
import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.etOrgnr;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OffentligKvartalsvisSykefraværshistorikkServiceTest {
    @Mock
    private KvartalsvisSykefraværRepository kvartalsvisSykefraværprosentRepository;

    OffentligKvartalsvisSykefraværshistorikkService offentligKvartalsvisSykefraværshistorikkService;

    @BeforeEach
    void setUp() {
        offentligKvartalsvisSykefraværshistorikkService =
                new OffentligKvartalsvisSykefraværshistorikkService(
                        new KvartalsvisSykefraværshistorikkService(
                                kvartalsvisSykefraværprosentRepository
                        ));
        when(kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand())
                .thenReturn(List.of(new SykefraværForEttKvartal(
                        new ÅrstallOgKvartal(2019, 1), new BigDecimal(50), new BigDecimal(100), 10)));
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void
    hentSykefraværshistorikk__skal_returnere_en_næring_dersom_virksomhet_er_i_bransjeprogram_på_2_siffer_nivå() {
        UnderenhetLegacy underenhet =
                new UnderenhetLegacy(etOrgnr(), etOrgnr(), "Underenhet AS", enNæringskode("10300"), 40);

        List<KvartalsvisSykefraværshistorikk> kvartalsvisSykefraværshistorikk =
                offentligKvartalsvisSykefraværshistorikkService.hentSykefraværshistorikkV1Offentlig(
                        underenhet);

        assertThatHistorikkHarKategori(kvartalsvisSykefraværshistorikk, Statistikkategori.NÆRING, true);
        assertThatHistorikkHarKategori(kvartalsvisSykefraværshistorikk, Statistikkategori.LAND, true);
        assertThatHistorikkHarKategori(
                kvartalsvisSykefraværshistorikk, Statistikkategori.BRANSJE, false);
    }

    @Test
    public void
    hentSykefraværshistorikk__skal_returnere_en_bransje_dersom_virksomhet_er_i_bransjeprogram_på_5_siffer_nivå() {
        UnderenhetLegacy underenhet =
                new UnderenhetLegacy(etOrgnr(), etOrgnr(), "Underenhet AS", enNæringskode("88911"), 40);

        List<KvartalsvisSykefraværshistorikk> kvartalsvisSykefraværshistorikk =
                offentligKvartalsvisSykefraværshistorikkService.hentSykefraværshistorikkV1Offentlig(
                        underenhet);

        assertThatHistorikkHarKategori(
                kvartalsvisSykefraværshistorikk, Statistikkategori.NÆRING, false);
        assertThatHistorikkHarKategori(kvartalsvisSykefraværshistorikk, Statistikkategori.LAND, true);
        assertThatHistorikkHarKategori(
                kvartalsvisSykefraværshistorikk, Statistikkategori.BRANSJE, true);
    }

    private static void assertThatHistorikkHarKategori(
            List<KvartalsvisSykefraværshistorikk> kvartalsvisSykefraværshistorikk,
            Statistikkategori statistikkategori,
            boolean expected) {
        assertThat(
                kvartalsvisSykefraværshistorikk.stream()
                        .anyMatch(historikk -> historikk.getType().equals(statistikkategori)))
                .isEqualTo(expected);
    }

}
