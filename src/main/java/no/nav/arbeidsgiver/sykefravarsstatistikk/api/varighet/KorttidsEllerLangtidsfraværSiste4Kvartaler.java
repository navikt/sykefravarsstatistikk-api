package no.nav.arbeidsgiver.sykefravarsstatistikk.api.varighet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// TODO classen heter SykefraværMedVarighetshistorikk men det er egentlig ikke noe "historikk" i det, bare samlet data om de siste 4 kvartalene
@Data
public class KorttidsEllerLangtidsfraværSiste4Kvartaler {
    private String langtidEllerKorttid; // langtid | korttid

    private Siste4KvartalerSykefravær siste4KvartalerSykefravær;
}


