package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import java.math.BigDecimal;

public class KafkaTopicValue {
    final String næringskode5Siffer;
    final int årstall;
    final int kvartal;
    final boolean erMaskert;
    final BigDecimal prosent;
    final BigDecimal tapteDagsverk;
    final BigDecimal muligeDagsverk;

    public KafkaTopicValue(String næringskode5Siffer, int årstall, int kvartal, boolean erMaskert, BigDecimal prosent, BigDecimal tapteDagsverk, BigDecimal muligeDagsverk) {
        this.næringskode5Siffer = næringskode5Siffer;
        this.årstall = årstall;
        this.kvartal = kvartal;
        this.erMaskert = erMaskert;
        this.prosent = prosent;
        this.tapteDagsverk = tapteDagsverk;
        this.muligeDagsverk = muligeDagsverk;
    }
}
