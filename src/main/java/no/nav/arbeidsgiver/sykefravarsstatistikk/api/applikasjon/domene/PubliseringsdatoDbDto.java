package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domene;

import java.sql.Date;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@EqualsAndHashCode
public class PubliseringsdatoDbDto {

  private final Integer rapportPeriode; // (sic)
  private final Date offentligDato;
  private final Date oppdatertDato;
  private final String aktivitet;

  public PubliseringsdatoDbDto(
      Integer rapportPeriode,
      Date offentligDato, // dato for offentliggjøring
      Date oppdatertDato,
      String aktivitet // beskrivelse, typ "Sykefravær pr 3. kvartal 2022"
      ) {
    this.rapportPeriode = rapportPeriode;
    this.offentligDato = offentligDato;
    this.oppdatertDato = oppdatertDato;
    this.aktivitet = aktivitet;
  }

  public int sammenlignPubliseringsdatoer(@NotNull PubliseringsdatoDbDto annen) {
    return this.getOffentligDato().toLocalDate().compareTo(annen.getOffentligDato().toLocalDate());
  }
}
