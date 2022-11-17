package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import static no.nav.arbeidsgiver.sykefravarsstatistikk.api.TestData.etÅrstallOgKvartal;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.eksportering.autoeksport.SykefraværFlereKvartalerForEksport;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefravar.SykefraværMedKategori;
import org.junit.jupiter.api.Test;

public class SykefraværForEttKvartalTest {

  @Test
  public void sykefraværprosent__uten_data_er_ikke_maskert() {
    assertThat(new SykefraværForEttKvartal(
        etÅrstallOgKvartal(),
        null,
        null,
        0
    ).isErMaskert()).isFalse();
  }

  @Test
  public void sykefraværprosent__equals_test() {
    SykefraværForEttKvartal sykefravær = new SykefraværForEttKvartal(
        etÅrstallOgKvartal(),
        new BigDecimal(5),
        new BigDecimal(10),
        20
    );
    assertThat(sykefravær.equals(null)).isEqualTo(false);
    assertThat(sykefravær.equals(new SykefraværForEttKvartal(
        etÅrstallOgKvartal(),
        new BigDecimal(5),
        new BigDecimal(10),
        20
    ))).isEqualTo(true);

    assertThat((new SykefraværForEttKvartal(
        etÅrstallOgKvartal(),
        null,
        null,
        4
    ).equals(sykefravær))).isEqualTo(false);

    assertThat(sykefravær.equals(new SykefraværForEttKvartal(
        etÅrstallOgKvartal(),
        null,
        null,
        4
    ))).isFalse();

  }

  @Test
  public void sykefraværFlereKvartalerForEksport_equals_test() {
    assertThat(Objects.equals(new SykefraværFlereKvartalerForEksport(
            List.of(
                new UmaskertSykefraværForEttKvartal(
                    etÅrstallOgKvartal(),
                    new BigDecimal(10),
                    new BigDecimal(100),
                    6
                ),
                new UmaskertSykefraværForEttKvartal(
                    etÅrstallOgKvartal().minusKvartaler(1),
                    new BigDecimal(12),
                    new BigDecimal(100),
                    6
                )
            )
        ),
        SykefraværFlereKvartalerForEksport.utenStatistikk())).isFalse();
    assertThat(new BigDecimal(1).equals(null)).isFalse();

  }

  @Test
  public void sykefraværMedKategori_equals_test() {
    assertThat(Objects.equals(new SykefraværMedKategori(
            Statistikkategori.VIRKSOMHET,
            "987654321",
            etÅrstallOgKvartal(),
            new BigDecimal(10),
            new BigDecimal(100),
            6
        ),
        SykefraværMedKategori.utenStatistikk(Statistikkategori.VIRKSOMHET, "987654321",
            etÅrstallOgKvartal()))).isFalse();

    assertThat(Objects.equals(new SykefraværMedKategori(
            Statistikkategori.VIRKSOMHET,
            "987654321",
            etÅrstallOgKvartal(),
            new BigDecimal(2),
            null,
            0
        ),
        SykefraværMedKategori.utenStatistikk(Statistikkategori.VIRKSOMHET, "987654321",
            etÅrstallOgKvartal()))).isTrue();

    assertThat(
        Objects.equals(
            SykefraværMedKategori.utenStatistikk(Statistikkategori.VIRKSOMHET, "987654321",
                etÅrstallOgKvartal()),
            SykefraværMedKategori.utenStatistikk(Statistikkategori.VIRKSOMHET, "987654321",
                etÅrstallOgKvartal())
        )).isTrue();
  }

  @Test
  public void sykefraværprosent__skal_regne_ut_riktig_prosent_ut_i_fra_tapte_og_mulige_dagsverk() {
    SykefraværForEttKvartal sykefravær = new SykefraværForEttKvartal(
        etÅrstallOgKvartal(),
        new BigDecimal(5),
        new BigDecimal(10),
        20
    );
    assertThat(sykefravær.getProsent()).isEqualTo(new BigDecimal("50.0"));
  }

  @Test
  public void sykefraværprosent__skal_runde_prosenten_opp_ved_tvil() {
    SykefraværForEttKvartal sykefravær = new SykefraværForEttKvartal(
        etÅrstallOgKvartal(),
        new BigDecimal(455),
        new BigDecimal(10000),
        100
    );
    assertThat(sykefravær.getProsent()).isEqualTo(new BigDecimal("4.6"));
  }

  @Test
  public void sykefraværprosent__skal_være_maskert_hvis_antallPersoner_er_4_eller_under() {
    SykefraværForEttKvartal sykefravær = new SykefraværForEttKvartal(
        etÅrstallOgKvartal(),
        new BigDecimal(1),
        new BigDecimal(10),
        4
    );
    assertThat(sykefravær.isErMaskert()).isTrue();
    assertThat(sykefravær.getProsent()).isNull();
    assertThat(sykefravær.getTapteDagsverk()).isNull();
    assertThat(sykefravær.getMuligeDagsverk()).isNull();
  }

  @Test
  public void sykefraværprosent__skal_være_maskert_hvis_antallPersoner_over_4() {
    SykefraværForEttKvartal sykefravær = new SykefraværForEttKvartal(
        etÅrstallOgKvartal(),
        new BigDecimal(1),
        new BigDecimal(10),
        5
    );
    assertThat(sykefravær.isErMaskert()).isFalse();
    assertThat(sykefravær.getProsent()).isNotNull();
    assertThat(sykefravær.getTapteDagsverk()).isNotNull();
    assertThat(sykefravær.getMuligeDagsverk()).isNotNull();
  }

  @Test
  @SneakyThrows
  public void sykefraværprosent__skal_bare_inkludere_relevante_felt_i_json_konvertering() {
    ObjectMapper mapper = new ObjectMapper();

    SykefraværForEttKvartal sykefravær = new SykefraværForEttKvartal(
        etÅrstallOgKvartal(),
        new BigDecimal(5),
        new BigDecimal(10),
        20
    );
    JsonNode json = mapper.readTree(mapper.writeValueAsString(sykefravær));
    JsonNode ønsketJson = mapper.readTree(
        "{" +
            "    \"årstall\": 2019," +
            "    \"kvartal\": 4," +
            "    \"prosent\": 50.0," +
            "    \"tapteDagsverk\": 5.0," +
            "    \"muligeDagsverk\": 10.0," +
            "    \"erMaskert\": false" +
            "}"
    );

    assertThat(json).isEqualTo(ønsketJson);
  }
}
