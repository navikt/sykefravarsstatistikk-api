package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AggregertStatistikkDto {

    // TODO: Jeg er identisk til feltet prosentSisteFireKvartalerTotalt.
    //  Fjern meg når Forebygge fravær har byttet til prosentSisteFireKvartalerTotalt.
    public List<StatistikkDto> prosentSiste4Kvartaler = List.of();

    public List<StatistikkDto> prosentSiste4KvartalerTotalt = List.of();
    public List<StatistikkDto> prosentSiste4KvartalerGradert = List.of();
    public List<StatistikkDto> prosentSiste4KvartalerKorttid = List.of();
    public List<StatistikkDto> prosentSiste4KvartalerLangtid = List.of();

    // TODO: Jeg er identisk til feltet trendTotalt.
    //  Fjern meg når Forebygge fravær har byttet til trendTotalt.
    public List<StatistikkDto> trend = List.of();

    public List<StatistikkDto> trendTotalt = List.of();
}
