package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer;

import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@EqualsAndHashCode
public class ImporttidspunktDto {

    private final Timestamp importertTidspunkt;
    private final String gjeldendeÅrstall;
    private final String gjeldendeKvartal;

    public ImporttidspunktDto(
          Timestamp importertTidspunkt,
          String gjeldendeÅrstall,
          String gjeldendeKvartal


    ) {
        this.importertTidspunkt = importertTidspunkt;
        this.gjeldendeÅrstall = gjeldendeÅrstall;
        this.gjeldendeKvartal = gjeldendeKvartal;
    }

    public LocalDate getImportertDato() {
        return this.getImportertTidspunkt().toLocalDateTime().toLocalDate();
    }
}
