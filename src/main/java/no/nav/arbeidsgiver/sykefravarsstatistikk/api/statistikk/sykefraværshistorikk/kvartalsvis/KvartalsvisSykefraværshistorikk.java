package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.kvartalsvis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartal;

import java.util.List;

@Data
public class KvartalsvisSykefraværshistorikk {
  private Statistikkategori type;
  private String label;

  @JsonProperty("kvartalsvisSykefraværsprosent")
  private List<SykefraværForEttKvartal> sykefraværForEttKvartal;
}
