package no.nav.arbeidsgiver.sykefravarsstatistikk.api.infrastruktur.kafka

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
open class KafkaUtsendingHistorikkRepository(
    @param:Qualifier("sykefravarsstatistikkJdbcTemplate") private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {
    @Async
    open fun opprettHistorikk(orgnr: String?, key: String?, value: String?) {
        val parametre: MutableMap<String, String?> = HashMap()
        parametre["orgnr"] = orgnr
        parametre["key"] = key
        parametre["value"] = value
        namedParameterJdbcTemplate.update(
            "insert into kafka_utsending_historikk (orgnr, key_json, value_json) "
                    + "values (:orgnr, :key, :value) ",
            parametre
        )
    }

    fun slettHistorikk(): Int {
        return namedParameterJdbcTemplate.update(
            "delete from kafka_utsending_historikk ", HashMap<String, Any?>()
        )
    }
}
