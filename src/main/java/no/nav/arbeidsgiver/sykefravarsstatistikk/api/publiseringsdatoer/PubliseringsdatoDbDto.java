package no.nav.arbeidsgiver.sykefravarsstatistikk.api.publiseringsdatoer;

import java.sql.Date;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class PubliseringsdatoDbDto {

    private final Integer rapportPeriode; // (sic)
    private final Date offentligDato;
    private final Date oppdatertDato;
    private final String aktivitet;


    public PubliseringsdatoDbDto(
          Integer rapportPeriode,
          Date offentligDato,
          Date oppdatertDato,
          // dato for offentliggjøring. todo: kanskje bruke LocalDateTime eller annen datotype her?
          String aktivitet // beskrivelse, typ "Sykefravær pr 3. kvartal 2022"
    ) {
        this.rapportPeriode = rapportPeriode;
        this.offentligDato = offentligDato;
        this.oppdatertDato = oppdatertDato;
        this.aktivitet = aktivitet;
    }
}
