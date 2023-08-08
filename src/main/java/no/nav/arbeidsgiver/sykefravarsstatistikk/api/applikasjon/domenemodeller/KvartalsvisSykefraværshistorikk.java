package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class KvartalsvisSykefraværshistorikk {
  private Statistikkategori type;
  private String label;

  @JsonProperty("kvartalsvisSykefraværsprosent")
  private List<SykefraværForEttKvartal> sykefraværForEttKvartal;
}
