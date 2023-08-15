package no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.eksportering;

import lombok.Data;
import lombok.Getter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.UmaskertSykefraværForEttKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.domenemodeller.ÅrstallOgKvartal;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.applikasjon.utils.StatistikkUtils;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.config.Konstanter;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Data
public class SykefraværFlereKvartalerForEksport {

    public BigDecimal tapteDagsverk;
    public BigDecimal muligeDagsverk;
    public BigDecimal prosent;
    public int antallPersoner;
    public List<ÅrstallOgKvartal> kvartaler;
    public final boolean erMaskert;

    public SykefraværFlereKvartalerForEksport(
            List<UmaskertSykefraværForEttKvartal> umaskertSykefravær) {
        erMaskert =
                !umaskertSykefravær.isEmpty()
                        && umaskertSykefravær.stream()
                        .allMatch(
                                v ->
                                        v.getAntallPersoner()
                                                < Konstanter
                                                .MIN_ANTALL_PERS_FOR_AT_STATISTIKKEN_IKKE_ER_PERSONOPPLYSNINGER);

        if (!erMaskert && !umaskertSykefravær.isEmpty()) {
            tapteDagsverk =
                    umaskertSykefravær.stream()
                            .map(UmaskertSykefraværForEttKvartal::getDagsverkTeller)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
            muligeDagsverk =
                    umaskertSykefravær.stream()
                            .map(UmaskertSykefraværForEttKvartal::getDagsverkNevner)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

            prosent =
                    StatistikkUtils.kalkulerSykefraværsprosent(this.tapteDagsverk, this.muligeDagsverk)
                            .getOrNull();
        } else {
            tapteDagsverk = null;
            muligeDagsverk = null;
            prosent = null;
        }

        kvartaler =
                umaskertSykefravær.stream()
                        .map(UmaskertSykefraværForEttKvartal::getårstallOgKvartal)
                        .collect(Collectors.toList());
        antallPersoner =
                umaskertSykefravær.isEmpty()
                        ? 0
                        : umaskertSykefravær.stream()
                        .max(Comparator.comparing(UmaskertSykefraværForEttKvartal::getårstallOgKvartal))
                        .get()
                        .getAntallPersoner();
    }

    public static SykefraværFlereKvartalerForEksport utenStatistikk() {
        return new SykefraværFlereKvartalerForEksport(List.of());
    }
}
