package no.nav.tag.sykefravarsstatistikk.api.provisjonering.synkronisering.integrasjon.utils;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public abstract class VirksomhetsklassifikasjonIntegrasjon {

    public static final String KODE = "kode";
    public static final String NAVN = "navn";
    public static final String NARINGSGRUPPE_KODE = "naringsgruppe_kode";

    final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public VirksomhetsklassifikasjonIntegrasjon(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }
}
