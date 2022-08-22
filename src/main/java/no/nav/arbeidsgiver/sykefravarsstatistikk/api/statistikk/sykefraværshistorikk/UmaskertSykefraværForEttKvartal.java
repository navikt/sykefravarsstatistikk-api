package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import static java.lang.Integer.max;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import lombok.Data;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.StatistikkUtils;
import org.jetbrains.annotations.NotNull;

@Data
public class UmaskertSykefraværForEttKvartal implements
        Comparable<UmaskertSykefraværForEttKvartal> {

    protected final BigDecimal tapteDagsverk;
    protected final BigDecimal muligeDagsverk;
    protected final int antallPersoner;
    @JsonIgnore
    private final ÅrstallOgKvartal årstallOgKvartal;

    public UmaskertSykefraværForEttKvartal(
            ÅrstallOgKvartal årstallOgKvartal,
            BigDecimal tapteDagsverk,
            BigDecimal muligeDagsverk,
            int antallPersoner) {
        this.årstallOgKvartal = årstallOgKvartal;
        this.tapteDagsverk = tapteDagsverk.setScale(1, RoundingMode.HALF_UP);
        this.muligeDagsverk = muligeDagsverk.setScale(1, RoundingMode.HALF_UP);
        this.antallPersoner = antallPersoner;
    }

    public UmaskertSykefraværForEttKvartal(
            ÅrstallOgKvartal kvartal,
            int tapteDagsverk,
            int muligeDagsverk,
            int antallPersoner) {
        this.årstallOgKvartal = kvartal;
        this.tapteDagsverk = new BigDecimal(String.valueOf(tapteDagsverk));
        this.muligeDagsverk = new BigDecimal(String.valueOf(muligeDagsverk));
        this.antallPersoner = antallPersoner;
    }

    public static Optional<UmaskertSykefraværForEttKvartal> hentUtKvartal(
            Collection<UmaskertSykefraværForEttKvartal> sykefravær,
            @NotNull ÅrstallOgKvartal kvartal) {
        return (sykefravær == null)
                ? Optional.empty()
                : sykefravær.stream()
                        .filter(datapunkt -> datapunkt.getÅrstallOgKvartal().equals(kvartal))
                        .findAny();
    }

    public int getKvartal() {
        return årstallOgKvartal != null ? årstallOgKvartal.getKvartal() : 0;
    }

    public int getÅrstall() {
        return årstallOgKvartal != null ? årstallOgKvartal.getÅrstall() : 0;
    }

    public BigDecimal kalkulerSykefraværsprosent() {
        return StatistikkUtils.kalkulerSykefraværsprosent(tapteDagsverk, muligeDagsverk);
    }

    public boolean harAntallMuligeDagsverkLikNull() {
        return muligeDagsverk.equals(BigDecimal.ZERO);
    }

    public UmaskertSykefraværForEttKvartal add(
            UmaskertSykefraværForEttKvartal sykefravær
    ) {
        if (!sykefravær.getÅrstallOgKvartal().equals(årstallOgKvartal)) {
            throw new IllegalArgumentException(
                    "Kan ikke summere kvartalsvis sykefravær med forskjellige kvartaler");
        }
        return new UmaskertSykefraværForEttKvartal(
                årstallOgKvartal,
                tapteDagsverk.add(sykefravær.getTapteDagsverk()),
                muligeDagsverk.add(sykefravær.getMuligeDagsverk()),
                antallPersoner + sykefravær.getAntallPersoner()
        );
    }

    @Override
    public int compareTo(@NotNull UmaskertSykefraværForEttKvartal kvartalsvisSykefravær) {
        return Comparator
                .comparing(UmaskertSykefraværForEttKvartal::getÅrstallOgKvartal)
                .compare(this, kvartalsvisSykefravær);
    }
}
