package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.sql.Date;

@Getter
@EqualsAndHashCode
public class PubliseringsdatoDbDto {

    private final Integer rapportPeriode; // (sic)
    private final Date offentligDato;
    private final Date oppdatertDato;
    private final String aktivitet;
    private Boolean erPublisert;


    public PubliseringsdatoDbDto(
          Integer rapportPeriode,
          Date offentligDato, // dato for offentliggjøring
          Date oppdatertDato,
          String aktivitet, // beskrivelse, typ "Sykefravær pr 3. kvartal 2022"
          Boolean erPublisert
    ) {
        this.rapportPeriode = rapportPeriode;
        this.offentligDato = offentligDato;
        this.oppdatertDato = oppdatertDato;
        this.aktivitet = aktivitet;
        this.erPublisert = erPublisert;
    }

    public int sammenlignOffentligDato(@NotNull PubliseringsdatoDbDto publiseringsdato) {
        return this.getOffentligDato().toLocalDate().compareTo(publiseringsdato.getOffentligDato().toLocalDate());
    }
}
