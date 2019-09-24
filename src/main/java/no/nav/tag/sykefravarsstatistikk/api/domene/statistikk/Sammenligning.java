package no.nav.tag.sykefravarsstatistikk.api.domene.statistikk;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Sammenligning {
    private int kvartal;
    private int årstall;
    private Sykefraværprosent virksomhet;
    private Sykefraværprosent næring;
    private Sykefraværprosent sektor;
    private Sykefraværprosent land;
}
