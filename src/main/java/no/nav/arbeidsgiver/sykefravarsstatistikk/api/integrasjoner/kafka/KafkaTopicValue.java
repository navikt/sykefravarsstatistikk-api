package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.sykefraværshistorikk.SykefraværForEttKvartal;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;

public class KafkaTopicValue {
    final int årstall;
    final int kvartal;
    final String næringskode5Siffer;
    final boolean erMaskert;
    final BigDecimal prosent;
    final BigDecimal tapteDagsverk;
    final BigDecimal muligeDagsverk;
    //Pair<Statistikkategori,SykefraværForEttKvartal> vikrsomhetSykefraværForETtKvartal;
    public final Pair<Statistikkategori,SykefraværForEttKvartal> næring5SifferSykefraværForEttKvartal;
    public final Pair<Statistikkategori,SykefraværForEttKvartal> næringSykefraværForEttKvartal;

    public KafkaTopicValue(int årstall, int kvartal, String næringskode5Siffer, boolean erMaskert, BigDecimal prosent, BigDecimal tapteDagsverk, BigDecimal muligeDagsverk, Pair<Statistikkategori, SykefraværForEttKvartal> næring5SifferSykefraværForEttKvartal, Pair<Statistikkategori, SykefraværForEttKvartal> næringSykefraværForEttKvartal) {
        this.årstall = årstall;
        this.kvartal = kvartal;
        this.næringskode5Siffer = næringskode5Siffer;
        this.erMaskert = erMaskert;
        this.prosent = prosent;
        this.tapteDagsverk = tapteDagsverk;
        this.muligeDagsverk = muligeDagsverk;
        this.næring5SifferSykefraværForEttKvartal = næring5SifferSykefraværForEttKvartal;
        this.næringSykefraværForEttKvartal = næringSykefraværForEttKvartal;
    }
}
