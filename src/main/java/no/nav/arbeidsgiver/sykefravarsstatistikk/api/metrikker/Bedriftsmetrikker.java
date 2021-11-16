package no.nav.arbeidsgiver.sykefravarsstatistikk.api.metrikker;

import lombok.Builder;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.Næringskode5Siffer;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.bransjeprogram.ArbeidsmiljøportalenBransje;

import java.math.BigDecimal;

@Data
@Builder
public class Bedriftsmetrikker {
    private BigDecimal antallAnsatte;
    private Næringskode5Siffer næringskode5Siffer;
    private ArbeidsmiljøportalenBransje bransje;
}
