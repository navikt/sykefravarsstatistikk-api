package no.nav.tag.sykefravarsstatistikk.api.domene;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlettOgOpprettResultat {

    private int antallRadSlettet;
    private int antallRadOpprettet;

    public void add(SlettOgOpprettResultat resultat) {
        antallRadOpprettet = antallRadOpprettet + resultat.getAntallRadOpprettet();
        antallRadSlettet = antallRadSlettet + resultat.getAntallRadSlettet();
    }
}
