package no.nav.tag.sykefravarsstatistikk.api.statistikk;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Sykefraværprosent {
    private final BigDecimal land;
}
