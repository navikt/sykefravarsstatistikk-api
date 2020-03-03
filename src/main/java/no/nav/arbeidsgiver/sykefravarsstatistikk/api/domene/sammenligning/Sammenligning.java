package no.nav.arbeidsgiver.sykefravarsstatistikk.api.domene.sammenligning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

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
