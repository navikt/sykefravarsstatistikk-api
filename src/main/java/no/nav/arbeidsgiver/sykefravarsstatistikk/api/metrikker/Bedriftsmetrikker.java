package no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker;

import lombok.Builder;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.enhetsregisteret.Næringskode5Siffer;

import java.math.BigDecimal;

@Data
@Builder
public class Bedriftsmetrikker {
    private BigDecimal antallAnsatte;
    private Næringskode5Siffer næringskode5Siffer;
}
