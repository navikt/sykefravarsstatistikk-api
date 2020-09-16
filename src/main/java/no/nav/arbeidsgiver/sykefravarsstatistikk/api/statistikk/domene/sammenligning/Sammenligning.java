package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.domene.sammenligning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Deprecated // TODO Brukes i Besøksstatistikk. Må vurdere om statistikken fremdeles skal lages, og i så fall knytte det til Sykefraværshistorikk-objektet.
@Data
@Builder
@AllArgsConstructor
public class Sammenligning {
    private int kvartal;
    private int årstall;
    private Sykefraværprosent virksomhet;
    private Sykefraværprosent næring;
    private Sykefraværprosent bransje;
    private Sykefraværprosent sektor;
    private Sykefraværprosent land;
}
