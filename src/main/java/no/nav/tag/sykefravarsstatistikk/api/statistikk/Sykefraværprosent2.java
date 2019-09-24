package no.nav.tag.sykefravarsstatistikk.api.statistikk;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Deprecated
@Data
@Builder
public class Sykefraværprosent2 {
    private final BigDecimal land;
}
