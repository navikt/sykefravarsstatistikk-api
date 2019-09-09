package no.nav.tag.sykefravarsstatistikk.api.sykefravarprosent;


import lombok.Builder;
import lombok.Data;
import no.nav.tag.sykefravarsstatistikk.api.domain.stats.LandStatistikk;

@Data
@Builder
public class Sykefraværprosent {

    private final LandStatistikk landStatistikk;
}
