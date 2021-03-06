package no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpprettEllerOppdaterResultat {

    private int antallRadOpprettet;
    private int antallRadOppdatert;

    public OpprettEllerOppdaterResultat add(OpprettEllerOppdaterResultat resultat) {
        antallRadOpprettet = antallRadOpprettet + resultat.getAntallRadOpprettet();
        antallRadOppdatert = antallRadOppdatert + resultat.getAntallRadOppdatert();
        return this;
    }
}
