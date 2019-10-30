package no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.utils;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public abstract class VirksomhetsklassifikasjonIntegrasjon {

    final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public VirksomhetsklassifikasjonIntegrasjon(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }
}
