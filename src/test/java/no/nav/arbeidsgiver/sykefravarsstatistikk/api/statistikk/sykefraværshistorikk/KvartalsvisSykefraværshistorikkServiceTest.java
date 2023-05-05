package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Sektor;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.SektorMappingService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværshistorikk;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis.KvartalsvisSykefraværshistorikkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

@ExtendWith(MockitoExtension.class)
public class KvartalsvisSykefraværshistorikkServiceTest {

  @Mock private KvartalsvisSykefraværRepository kvartalsvisSykefraværprosentRepository;
  @Mock private EnhetsregisteretClient enhetsregisteretClient;
  @Mock private SektorMappingService sektorMappingService;
  @Mock private KlassifikasjonerRepository klassifikasjonerRepository;

  KvartalsvisSykefraværshistorikkService kvartalsvisSykefraværshistorikkService;

  @BeforeEach
  public void setUp() {
    kvartalsvisSykefraværshistorikkService =
        new KvartalsvisSykefraværshistorikkService(
            kvartalsvisSykefraværprosentRepository,
            sektorMappingService,
            klassifikasjonerRepository);

    when(sektorMappingService.mapTilSSBSektorKode(any()))
        .thenReturn(new Sektor("1", "Statlig forvaltning"));
    when(kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand())
        .thenReturn(Arrays.asList(sykefraværprosent("Norge")));
    when(kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentSektor(any()))
        .thenReturn(Arrays.asList(sykefraværprosent("Statlig forvlatning")));
    when(kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentVirksomhet(any()))
        .thenReturn(Arrays.asList(sykefraværprosent("Test Virksomhet")));
  }

  @Test
  public void hentSykefraværshistorikk_skal_ikke_feile_dersom_uthenting_av_en_type_data_feiler() {
    when(klassifikasjonerRepository.hentNæring(any()))
        .thenThrow(new EmptyResultDataAccessException(1));

    List<KvartalsvisSykefraværshistorikk> kvartalsvisSykefraværshistorikk =
        kvartalsvisSykefraværshistorikkService.hentSykefraværshistorikk(
            enUnderenhet("999999998"), enInstitusjonellSektorkode());

    verify(kvartalsvisSykefraværprosentRepository, times(0))
        .hentKvartalsvisSykefraværprosentNæring(any());
    KvartalsvisSykefraværshistorikk næringSFHistorikk = kvartalsvisSykefraværshistorikk.get(2);
    assertThat(næringSFHistorikk.getLabel()).isNull();
  }

  @Test
  public void
      hentSykefraværshistorikk__skal_returnere_en_næring_dersom_virksomhet_er_i_bransjeprogram_på_2_siffer_nivå() {
    Underenhet underenhet =
        new Underenhet(etOrgnr(), etOrgnr(), "Underenhet AS", enNæringskode5Siffer("10300"), 40);

    when(klassifikasjonerRepository.hentNæring(any()))
        .thenReturn(new Næring("10", "Produksjon av nærings- og nytelsesmidler"));

    List<KvartalsvisSykefraværshistorikk> kvartalsvisSykefraværshistorikk =
        kvartalsvisSykefraværshistorikkService.hentSykefraværshistorikk(
            underenhet, enInstitusjonellSektorkode());

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

  private static SykefraværForEttKvartal sykefraværprosent(String label) {
    return new SykefraværForEttKvartal(
        new ÅrstallOgKvartal(2019, 1), new BigDecimal(50), new BigDecimal(100), 10);
  }
}
