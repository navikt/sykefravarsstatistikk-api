package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import no.nav.arbeidsgiver.sykefravarsstatistikk.api.statistikk.Statistikkategori;

@Getter
@AllArgsConstructor
public class KafkaStatistikkategoriTopicKey {
    Statistikkategori kategori;
    String kode;
    int kvartal;
    int årstall;
}
