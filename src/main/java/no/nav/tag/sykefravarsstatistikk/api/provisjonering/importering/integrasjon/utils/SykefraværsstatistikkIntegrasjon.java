package no.nav.tag.sykefravarsstatistikk.api.provisjonering.importering.integrasjon.utils;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public abstract class SykefraværsstatistikkIntegrasjon {

    final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public SykefraværsstatistikkIntegrasjon(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }
}
