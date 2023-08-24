package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.aggregering.KvartalsvisSykefraværshistorikkService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.*;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.KlassifikasjonerRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.database.KvartalsvisSykefraværRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.math.BigDecimal;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KvartalsvisSykefraværshistorikkServiceTest {

  @Mock private KvartalsvisSykefraværRepository kvartalsvisSykefraværprosentRepository;
  @Mock private KlassifikasjonerRepository klassifikasjonerRepository;

  KvartalsvisSykefraværshistorikkService kvartalsvisSykefraværshistorikkService;

  @BeforeEach
  public void setUp() {
    kvartalsvisSykefraværshistorikkService =
        new KvartalsvisSykefraværshistorikkService(
            kvartalsvisSykefraværprosentRepository,
            klassifikasjonerRepository);

    when(EnhetsregisteretSektorMappingKt.fraEnhetsregisteretSektor(any()))
        .thenReturn(new Sektor("1"));
    when(kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand())
        .thenReturn(List.of(sykefraværprosent()));
    when(kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentSektor(any()))
        .thenReturn(List.of(sykefraværprosent()));
    when(kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentVirksomhet(any()))
        .thenReturn(List.of(sykefraværprosent()));
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
    UnderenhetLegacy underenhet =
        new UnderenhetLegacy(etOrgnr(), etOrgnr(), "Underenhet AS", enNæringskode5Siffer("10300"), 40);

    when(klassifikasjonerRepository.hentNæring(any()))
        .thenReturn(new Næring("10", "Produksjon av nærings- og nytelsesmidler"));

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
