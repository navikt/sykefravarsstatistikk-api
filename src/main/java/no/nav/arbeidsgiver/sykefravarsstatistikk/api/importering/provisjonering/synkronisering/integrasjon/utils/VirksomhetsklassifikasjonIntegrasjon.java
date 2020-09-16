package no.nav.arbeidsgiver.sykefravarsstatistikk.api.importering.provisjonering.synkronisering.integrasjon.utils;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public abstract class VirksomhetsklassifikasjonIntegrasjon {

    public static final String KODE = "kode";
    public static final String NAVN = "navn";

    final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public VirksomhetsklassifikasjonIntegrasjon(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }
}
