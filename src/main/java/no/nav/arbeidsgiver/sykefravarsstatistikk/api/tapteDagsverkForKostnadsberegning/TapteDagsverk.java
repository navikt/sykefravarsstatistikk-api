package no.nav.arbeidsgiver.sykefravarsstatistikk.api.tapteDagsverkForKostnadsberegning;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class TapteDagsverk {
    private BigDecimal tapteDagsverk;
    private boolean erMaskert;
}
