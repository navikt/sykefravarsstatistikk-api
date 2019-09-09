package no.nav.tag.sykefravarsstatistikk.api.sykefravarprosent;


import lombok.Builder;
import lombok.Data;
import no.nav.tag.sykefravarsstatistikk.api.domain.stats.LandStatistikk;

import java.math.BigDecimal;

@Data
@Builder
public class Sykefraværprosent {
    private final BigDecimal land;
}
