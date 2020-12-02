package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import lombok.Getter;
import lombok.ToString;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Varighetskategori;

import java.math.BigDecimal;

@Getter
@ToString(callSuper = true)
public class UmaskertSykefraværForEttKvartalForGradertSykemelding extends UmaskertSykefraværForEttKvartal {

    private int antallSykemeldinger;
    private int antallGraderteSykemeldinger;
    private BigDecimal tapteDagsverkGradertSykemelding;

    public UmaskertSykefraværForEttKvartalForGradertSykemelding(
            ÅrstallOgKvartal årstallOgKvartal,
            int antallGraderteSykemeldinger,
            BigDecimal tapteDagsverkGradertSykemelding,
            int antallSykemeldinger,
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int antallPersoner
    ) {
        super(årstallOgKvartal, tapteDagsverk, muligeDagsverk, antallPersoner);
        this.antallSykemeldinger = antallSykemeldinger;
        this.antallGraderteSykemeldinger = antallGraderteSykemeldinger;
        this.tapteDagsverkGradertSykemelding = tapteDagsverkGradertSykemelding;
    }

    public UmaskertSykefraværForEttKvartal tilUmaskertSykefraværForEttKvartal() {
        return new UmaskertSykefraværForEttKvartal(
                super.getÅrstallOgKvartal(),
                super.getTapteDagsverk(),
                super.getMuligeDagsverk(),
                super.getAntallPersoner());
    }
}
