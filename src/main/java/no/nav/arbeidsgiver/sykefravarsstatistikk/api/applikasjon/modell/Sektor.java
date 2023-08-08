package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.modell;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Sektor implements Virksomhetsklassifikasjon {
  private String kode;
  private String navn;
}
