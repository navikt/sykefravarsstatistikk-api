package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@DependsOn({"sykefravarsstatistikkJdbcTemplate"})
public class KafkaUtsendingHistorikkRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public KafkaUtsendingHistorikkRepository(@Qualifier("sykefravarsstatistikkJdbcTemplate")
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
                "insert into kafka_utsending_historikk (orgnr, key_json, value_json) " +
                        "values (:orgnr, :key, :value) ",
                parametre
        );
    }

}
