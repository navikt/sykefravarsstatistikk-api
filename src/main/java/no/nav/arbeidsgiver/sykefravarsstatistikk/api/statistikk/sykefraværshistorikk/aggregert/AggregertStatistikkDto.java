package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AggregertStatistikkDto {

    public List<StatistikkDto> prosentSiste4KvartalerTotalt = List.of();
    public List<StatistikkDto> prosentSiste4KvartalerGradert = List.of();
    public List<StatistikkDto> prosentSiste4KvartalerKorttid = List.of();
    public List<StatistikkDto> prosentSiste4KvartalerLangtid = List.of();

    public List<StatistikkDto> trendTotalt = List.of();

    public List<StatistikkDto> tapteDagsverkTotalt = List.of();
    public List<StatistikkDto> muligeDagsverkTotalt = List.of();
}
