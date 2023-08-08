package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class KafkaUtsendingHistorikkRepository {

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  public KafkaUtsendingHistorikkRepository(
      @Qualifier("sykefravarsstatistikkJdbcTemplate")
          NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
  }

  @Async
  public void opprettHistorikk(String orgnr, String key, String value) {
    Map<String, String> parametre = new HashMap<>();
    parametre.put("orgnr", orgnr);
    parametre.put("key", key);
    parametre.put("value", value);

    namedParameterJdbcTemplate.update(
        "insert into kafka_utsending_historikk (orgnr, key_json, value_json) "
            + "values (:orgnr, :key, :value) ",
        parametre);
  }

  public int slettHistorikk() {
    int antallSlettet =
        namedParameterJdbcTemplate.update(
            "delete from kafka_utsending_historikk ", new HashMap<>());
    return antallSlettet;
  }
}
