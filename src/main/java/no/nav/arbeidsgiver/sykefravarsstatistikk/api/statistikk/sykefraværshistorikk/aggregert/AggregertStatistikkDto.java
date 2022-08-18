package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.aggregert;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AggregertStatistikkDto {

    // TODO: Jeg er identisk som prosentSisteFireKvartalerTotalt.
    //  Fjern meg når Forebygge fravær har byttet til feltet prosentSisteFireKvartalerTotalt.
    public List<StatistikkDto> prosentSiste4Kvartaler = List.of();

    public List<StatistikkDto> prosentSiste4KvartalerTotalt = List.of();
    public List<StatistikkDto> prosentSiste4KvartalerGradert = List.of();
    public List<StatistikkDto> prosentSiste4KvartalerKorttid = List.of();
    public List<StatistikkDto> prosentSiste4KvartalerLangtid = List.of();
    public List<StatistikkDto> trend = List.of();
}
