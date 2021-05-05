package no.nav.arbeidsgiver.sykefravarsstatistikk.api.integrasjoner.kafka;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class KafkaUtsendingHistorikkRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public KafkaUtsendingHistorikkRepository(@Qualifier("sykefravarsstatistikkJdbcTemplate")
                                                     NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }


    @Async
    public void opprettHistorikk(String orgnr, String key, String value) {
        SqlParameterSource parametre =
                new MapSqlParameterSource();

        namedParameterJdbcTemplate.update(
                "insert into kafka_utsending_historikk (orgnr, key_json, value_json) " +
                        "values " +
                        "(:orgnr, :key, :value) ",
                parametre
        );
    }

}
