package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import java.math.BigDecimal;

public class KafkaTopicValue {
    final int årstall;
    final int kvartal;
    final boolean erMaskert;
    final BigDecimal prosent;
    final BigDecimal tapteDagsverk;
    final BigDecimal muligeDagsverk;

    public KafkaTopicValue( int årstall, int kvartal, boolean erMaskert, BigDecimal prosent, BigDecimal tapteDagsverk, BigDecimal muligeDagsverk) {
        this.årstall = årstall;
        this.kvartal = kvartal;
        this.erMaskert = erMaskert;
        this.prosent = prosent;
        this.tapteDagsverk = tapteDagsverk;
        this.muligeDagsverk = muligeDagsverk;
    }
}
