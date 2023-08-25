package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering.KvartalsvisSykefraværshistorikkService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.KvartalsvisSykefraværRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KvartalsvisSykefraværshistorikkServiceTest {

    @Mock
    private KvartalsvisSykefraværRepository kvartalsvisSykefraværprosentRepository;

    KvartalsvisSykefraværshistorikkService kvartalsvisSykefraværshistorikkService;

    @BeforeEach
    public void setUp() {
        kvartalsvisSykefraværshistorikkService =
                new KvartalsvisSykefraværshistorikkService(
                        kvartalsvisSykefraværprosentRepository
                );

        when(kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand())
                .thenReturn(List.of(sykefraværprosent()));
        when(kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentSektor(any()))
                .thenReturn(List.of(sykefraværprosent()));
        when(kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentVirksomhet(any()))
                .thenReturn(List.of(sykefraværprosent()));
    }

    @Test
    public void
    hentSykefraværshistorikk__skal_returnere_en_næring_dersom_virksomhet_er_i_bransjeprogram_på_2_siffer_nivå() {
        UnderenhetLegacy underenhet =
                new UnderenhetLegacy(etOrgnr(), etOrgnr(), "Underenhet AS", enNæringskode("10300"), 40);

        List<KvartalsvisSykefraværshistorikk> kvartalsvisSykefraværshistorikk =
                kvartalsvisSykefraværshistorikkService.hentSykefraværshistorikk(
                        underenhet, Sektor.PRIVAT);

        assertThatHistorikkHarKategori(kvartalsvisSykefraværshistorikk, Statistikkategori.NÆRING, true);
        assertThatHistorikkHarKategori(kvartalsvisSykefraværshistorikk, Statistikkategori.LAND, true);
        assertThatHistorikkHarKategori(kvartalsvisSykefraværshistorikk, Statistikkategori.SEKTOR, true);
        assertThatHistorikkHarKategori(
                kvartalsvisSykefraværshistorikk, Statistikkategori.VIRKSOMHET, true);
        assertThatHistorikkHarKategori(
                kvartalsvisSykefraværshistorikk, Statistikkategori.BRANSJE, false);
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

    private static SykefraværForEttKvartal sykefraværprosent() {
        return new SykefraværForEttKvartal(
                new ÅrstallOgKvartal(2019, 1), new BigDecimal(50), new BigDecimal(100), 10);
    }
}
