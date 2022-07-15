package no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk;

import static java.lang.Integer.max;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.felles.ÅrstallOgKvartal;
import org.jetbrains.annotations.NotNull;

@Getter
@EqualsAndHashCode
@ToString
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

    public static UmaskertSykefraværForEttKvartal tomtUmaskertKvartalsvisSykefravær(
            ÅrstallOgKvartal årstallOgKvartal) {
        return new UmaskertSykefraværForEttKvartal(årstallOgKvartal, new BigDecimal(0),
                new BigDecimal(0), 0);
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

    public BigDecimal getProsent() {
        return getTapteDagsverk().divide(getMuligeDagsverk()).multiply(new BigDecimal(100));
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
                max(antallPersoner, sykefravær.getAntallPersoner())
        );
    }

    @Override
    public int compareTo(@NotNull UmaskertSykefraværForEttKvartal kvartalsvisSykefravær) {
        return Comparator
                .comparing(UmaskertSykefraværForEttKvartal::getÅrstallOgKvartal)
                .compare(this, kvartalsvisSykefravær);
    }
}
