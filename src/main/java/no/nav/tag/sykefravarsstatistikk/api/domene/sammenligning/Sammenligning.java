package no.nav.tag.sykefravarsstatistikk.api.domene.sammenligning;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    public int getArstall() {
        return årstall;
    }

    @JsonIgnore
    public Sykefraværprosent getNaring() {
        return næring;
    }
}
