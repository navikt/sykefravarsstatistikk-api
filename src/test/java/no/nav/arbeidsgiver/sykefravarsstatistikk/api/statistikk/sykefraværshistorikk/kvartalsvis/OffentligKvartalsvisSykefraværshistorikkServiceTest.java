package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næring;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Underenhet;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.Bransjeprogram;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.enhetsregisteret.EnhetsregisteretClient;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.KlassifikasjonerRepository;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.SektorMappingService;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OffentligKvartalsvisSykefraværshistorikkServiceTest {
  @Mock private KvartalsvisSykefraværRepository kvartalsvisSykefraværprosentRepository;
  @Mock private EnhetsregisteretClient enhetsregisteretClient;
  @Mock private SektorMappingService sektorMappingService;
  @Mock private KlassifikasjonerRepository klassifikasjonerRepository;

  OffentligKvartalsvisSykefraværshistorikkService offentligKvartalsvisSykefraværshistorikkService;

  @BeforeEach
  void setUp() {
    offentligKvartalsvisSykefraværshistorikkService =
        new OffentligKvartalsvisSykefraværshistorikkService(
            new KvartalsvisSykefraværshistorikkService(
                kvartalsvisSykefraværprosentRepository,
                sektorMappingService,
                klassifikasjonerRepository,
                new Bransjeprogram()),
            new Bransjeprogram());
    when(kvartalsvisSykefraværprosentRepository.hentKvartalsvisSykefraværprosentLand())
        .thenReturn(Arrays.asList(sykefraværprosent("Norge")));
  }

  @AfterEach
  void tearDown() {}

  @Test
  void
      hentSykefraværshistorikkV1_offentlig_skal_ikke_feile_dersom_uthenting_av_en_type_data_feiler() {
    when(klassifikasjonerRepository.hentNæring(any()))
        .thenThrow(new EmptyResultDataAccessException(1));

    List<KvartalsvisSykefraværshistorikk> kvartalsvisSykefraværshistorikk =
        offentligKvartalsvisSykefraværshistorikkService.hentSykefraværshistorikkV1Offentlig(
            enUnderenhet("999999998"));

    verify(kvartalsvisSykefraværprosentRepository, times(0))
        .hentKvartalsvisSykefraværprosentNæring(any());
    KvartalsvisSykefraværshistorikk næringSFHistorikk = kvartalsvisSykefraværshistorikk.get(1);
    assertThat(næringSFHistorikk.getLabel()).isNull();
  }

  @Test
  public void
      hentSykefraværshistorikk__skal_returnere_en_næring_dersom_virksomhet_er_i_bransjeprogram_på_2_siffer_nivå() {
    Underenhet underenhet =
        Underenhet.builder()
            .orgnr(etOrgnr())
            .overordnetEnhetOrgnr(etOrgnr())
            .navn("Underenhet AS")
            .næringskode(enNæringskode5Siffer("10300"))
            .antallAnsatte(40)
            .build();

    when(klassifikasjonerRepository.hentNæring(any()))
        .thenReturn(new Næring("10", "Produksjon av nærings- og nytelsesmidler"));

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
    Underenhet underenhet =
        Underenhet.builder()
            .orgnr(etOrgnr())
            .overordnetEnhetOrgnr(etOrgnr())
            .navn("Underenhet AS")
            .næringskode(enNæringskode5Siffer("88911"))
            .antallAnsatte(40)
            .build();

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

  private static SykefraværForEttKvartal sykefraværprosent(String label) {
    return new SykefraværForEttKvartal(
        new ÅrstallOgKvartal(2019, 1), new BigDecimal(50), new BigDecimal(100), 10);
  }
}
