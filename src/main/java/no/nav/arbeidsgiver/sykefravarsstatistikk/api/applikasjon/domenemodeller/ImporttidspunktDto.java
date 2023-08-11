package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller;

import java.sql.Timestamp;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class ImporttidspunktDto {

  private final Timestamp sistImportertTidspunkt;
  private final ÅrstallOgKvartal gjeldendePeriode;

  public ImporttidspunktDto(Timestamp sistImportertTidspunkt, ÅrstallOgKvartal gjeldendePeriode) {
    this.sistImportertTidspunkt = sistImportertTidspunkt;
    this.gjeldendePeriode = gjeldendePeriode;
  }

  public LocalDate getImportertDato() {
    return sistImportertTidspunkt.toLocalDateTime().toLocalDate();
  }
}