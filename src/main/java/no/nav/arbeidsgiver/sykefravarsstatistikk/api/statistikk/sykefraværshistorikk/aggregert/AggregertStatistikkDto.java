package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Setter
public class AggregertStatistikkDto {
    public List<StatistikkDto> prosentSiste4Kvartaler = List.of();
    public List<StatistikkDto> trend = List.of();
}
